#! /bin/bash

if [ ! -f modules.cfg ]; then
    cp modules.template.cfg modules.cfg
    echo "Using default modules.cfg. To enable/disable modules, edit manually."
fi
MODULES=$(cat modules.cfg | grep -v "^[# ]" | grep . | tr -s ' ')

git --version 1>/dev/null 2>/dev/null || {
    echo >&2 'ERROR: Git not found. Please install Git, for example with "sudo apt install git".'
    exit 1
}

mvn --version 1>/dev/null 2>/dev/null || {
    echo >&2 'ERROR: Maven not found. Please install Maven, for example with "sudo apt install maven".'
    exit 1
}

tac() {
    local lines i
    readarray -t lines
    for (( i = ${#lines[@]}; i--; )); do
        printf '%s\n' "${lines[i]}"
    done
}

foreach-module() {
    if [ ! -z "$MODULES" ]; then
        while read -r line; do
            name=$(echo $line | cut -d' ' -f1)
            main=$(echo $line | cut -d' ' -f2)
            commit=$(echo $line | cut -d' ' -f3)
            $1 $name $main $commit
        done <<<"$MODULES"
    fi
}

pom-module() {
    echo "		<module>$1</module>"
}

status-module() {
    if [ -d $1/.git ] || [ -L $1 ]; then
        echo "Status of module $1"
        git -C $1 status -bs | sed 's/^/  /'
    else
        echo >&2 "ERROR: Module $1 does not exist"
    fi
}

diff-module() {
    if [ -d $1/.git ] || [ -L $1 ]; then
        echo "Diff of module $1"
        git -C $1 diff
    else
        echo >&2 "ERROR: Module $1 does not exist"
    fi
}

clone-module() {
    if [ ! -d $1/.git ] && [ ! -L $1 ]; then
        echo "Cloning module $1 from remote $2"
        git clone --recurse-submodules -j8 $2 -q
        if [[ "$?" -ne 0 ]]; then
            echo >&2 "ERROR: Could not clone module $1"
            exit 1
        fi
      if [ ! -z $3 ]; then
        echo "Checking out $3 in module $1"
        git -C $1 checkout $3
      fi
    fi
}

pull-module() {
    if [ -d $1/.git ] || [ -L $1 ]; then
        echo "Pulling module $1"
        git -C $1 pull -q
        if [[ "$?" -ne 0 ]]; then
            echo >&2 "ERROR: Could not pull module $1"
            exit 1
        fi
    else
        clone-module $1 $2 $3
    fi
}

push-module() {
    if [ -d $1/.git ] || [ -L $1 ]; then
        echo "Pushing module $1"
        git -C $1 push -q
        if [[ "$?" -ne 0 ]]; then
            echo >&2 "ERROR: Could not push module $1"
            exit 1
        fi
    else
        echo >&2 "ERROR: Module $1 does not exist"
        exit 1
    fi
}

pom() {
    {
        cat pom.template.xml | sed "/<modules>/q"
        foreach-module pom-module
        tac < pom.template.xml | sed "/<\/modules>/q" | tac
    } >pom.xml
}

status() {
    if [ -z $1 ]; then
        foreach-module status-module
        status-module .
    else
        status-module $1
    fi
}

diff() {
    if [ -z $1 ]; then
        foreach-module diff-module
        diff-module .
    else
        diff-module $1
    fi
}

clone() {
    foreach-module clone-module
}

pull() {
    if [ -z $1 ]; then
        foreach-module pull-module
        pull-module .
    else
        pull-module $1
    fi
}

push() {
    if [ -z $1 ]; then
        foreach-module push-module
        push-module .
    else
        push-module $1
    fi
}

maven-args() {
    if [ -z $1 ]; then
        module=.
        args=
    else
        module=$1
        shift
        args=$@
    fi
    echo -f $module $args
}

clean() {
    mvn clean $(maven-args "$@")
}

install() {
    mvn install $(maven-args "$@")
}

inst() {
    mvn -T 1C install -Dmaven.test.skip -DskipTests -Dmaven.javadoc.skip=true $(maven-args "$@")
}

format() {
    mvn formatter:format -fn $(maven-args "$@")
}

license-header() {
    python3 scripts/license-header.py $1
}

docker() {
    if [ -z $1 ]; then
        echo >&2 "ERROR: No module supplied"
        exit 1
    else
        module=$1
        module_prefix=$(echo $module | cut -d/ -f1)
        {
            cat scripts/Dockerfile.template | sed "/{MODULES}/q" | head -n-1
            DOCKER_MODULES=$(cat modules.cfg | grep -v "^[# ]" | grep -v "^$module_prefix" | grep -v -e '^$')
            echo "RUN echo \\"
            while read -r line; do
                name=$(echo $line | cut -d' ' -f1)
                main=$(echo $line | cut -d' ' -f2)
                echo "  && echo $name $main $(git -C $name rev-parse HEAD) >> modules.cfg \\"
            done <<<"$DOCKER_MODULES"
            echo "  && echo"
            tac < scripts/Dockerfile.template | sed "/{MODULES}/q" | tac | tail -n+2
            if [ -f $module/*-all.jar ]; then
                echo ENTRYPOINT [\"java\", \"-jar\", \"$(basename $(ls $module/*-all.jar))\"]
            fi
        } >$module/Dockerfile
        sed -i "s#{MODULE}#$module#" $module/Dockerfile
        sed -i "s#{COMMIT}#$(git rev-parse HEAD)#" $module/Dockerfile
    fi
}

default() {
    install
}

help() {
    echo "Usage: $0 [-h] [command[:module][:arg...] ...]" 1>&2
    echo "Commands:"
    echo "  help                          Show script usage"
    echo "  clone                         Clone all enabled modules"
    echo "  pom                           Generate Maven POM for root module"
    echo "  status[:module]               Print status of module"
    echo "  diff[:module]                 Print diff of module"
    echo "  pull[:module]                 Pull module"
    echo "  push[:module]                 Push module"
    echo "  clean[:module][:arg...]       Clean build artifacts"
    echo "  install[:module][:arg...]     Build module"
    echo "  inst[:module][:arg...]        Build module, skipping tests and documentation"
    echo "  format[:module][:arg...]      Format a module's Java source files"
    echo "  license-header[:module]       Write license headers for a module's Java source files"
    echo "  docker[:module]               Generate Dockerfile for module"
    echo "If no module is passed, a command applies to all enabled modules (as specified in modules.cfg)."
    echo "By default, \"install\" is invoked."
    echo "Also, \"clone\" and \"pom\" are always invoked."
    exit 0
}

if [[ $# -eq 0 ]]; then
    COMMANDS=default
else
    COMMANDS=$@
fi

while getopts ":h" o; do
    case "${o}" in
    *) help ;;
    esac
done
shift $((OPTIND - 1))

clone
pom

if (($OPTIND == 1)); then
    for cmd in $COMMANDS; do
        $(echo $cmd | tr : ' ')
    done
fi
