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

foreach-module() {
    if [ ! -z "$MODULES" ]; then
        while read -r line; do
            name=$(echo $line | cut -d' ' -f1)
            main=$(echo $line | cut -d' ' -f2)
            fallback=$(echo $line | cut -d' ' -f3)
            $1 $name $main $fallback
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

clone-module() {
    if [ ! -d $1/.git ] && [ ! -L $1 ]; then
        echo "Cloning module $1 from remote $2"
        git clone --recurse-submodules -j8 $2 -q
        if [[ "$?" -ne 0 ]]; then
            echo "Cloning module $1 from fallback remote $2"
            git clone --recurse-submodules -j8 $3 -q
            if [[ "$?" -ne 0 ]]; then
                echo >&2 "ERROR: Could not clone module $1"
                exit 1
            fi
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
        tac pom.template.xml | sed "/<\/modules>/q" | tac
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

clone() {
    if [ -z $1 ]; then
        foreach-module clone-module
    else
        clone-module $1
    fi
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

clean() {
    if [ -z $1 ]; then
        module=.
    else
        module=$1
    fi
    mvn clean $module
}

install() {
    if [ -z $1 ]; then
        module=.
    else
        module=$1
    fi
    mvn install -f $module
}

inst() {
    if [ -z $1 ]; then
        module=.
    else
        module=$1
    fi
    mvn -T 1C install -Dmaven.test.skip -DskipTests -Dmaven.javadoc.skip=true -f $module
}

default() {
    install
}

help() {
    echo "Usage: $0 [-h] [command[:module] ...]" 1>&2
    echo "Commands:"
    echo "  pom                 Generate Maven POM for root project"
    echo "  status[:module]     Print status of module"
    echo "  clone[:module]      Clone module"
    echo "  pull[:module]       Pull module"
    echo "  push[:module]       Push module"
    echo "  clean[:module]      Clean build artifacts with Maven"
    echo "  install[:module]    Build module with Maven"
    echo "  inst[:module]       Build module with Maven, skipping tests and documentation"
    echo "If no module is passed, a command applies to all enabled modules."
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
    h) help ;;
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
