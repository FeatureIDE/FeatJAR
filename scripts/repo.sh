#!/bin/bash
# helper script for handling typical tasks on repositories

git --version 1>/dev/null 2>/dev/null || {
    echo >&2 'ERROR: Git not found - please install Git, for example with "sudo apt install git"'
    exit 1
}

tac() {
    local lines i
    readarray -t lines
    for (( i = ${#lines[@]}; i--; )); do
        printf '%s\n' "${lines[i]}"
    done
}

foreach-repo() {
    for d in */; do
        ! [ -d $d/.git ] && continue
        $1 $(basename $d)
    done
}

status-repo() {
    if [ -d $1/.git ]; then
        echo "Status of module $1"
        git -C $1 status -bs | sed 's/^/  /'
    else
        echo >&2 "ERROR: Module $1 does not exist"
    fi
}

diff-repo() {
    if [ -d $1/.git ]; then
        echo "Diff of module $1"
        git -C $1 diff
    else
        echo >&2 "ERROR: Module $1 does not exist"
    fi
}

pull-repo() {
    if [ -d $1/.git ]; then
        echo "Pulling module $1"
        git -C $1 pull -q
        if [[ "$?" -ne 0 ]]; then
            echo >&2 "ERROR: Could not pull module $1"
            exit 1
        fi
    else
        echo >&2 "ERROR: Module $1 does not exist"
        exit 1
    fi
}

push-repo() {
    if [ -d $1/.git ]; then
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

status() {
    if [ -z $1 ]; then
        foreach-repo status-repo
        status-repo .
    else
        status-repo $1
    fi
}

diff() {
    if [ -z $1 ]; then
        foreach-repo diff-repo
        diff-repo .
    else
        diff-repo $1
    fi
}

pull() {
    if [ -z $1 ]; then
        foreach-repo pull-repo
        pull-repo .
    else
        pull-repo $1
    fi
}

push() {
    if [ -z $1 ]; then
        foreach-repo push-repo
        push-repo .
    else
        push-repo $1
    fi
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

help() {
    echo "Usage: $0 [-h] [command[:repo][:arg...] ...]" 1>&2
    echo "Commands:"
    echo "  help                          Show script usage"
    echo "  status[:repo]                 Print status of repository"
    echo "  diff[:repo]                   Print diff of repository"
    echo "  pull[:repo]                   Pull repository"
    echo "  push[:repo]                   Push repository"
    echo "  format[:repo][:arg...]        Format a repository's Java source files"
    echo "  license-header[:repo]         Write license headers for a repository's Java source files"
    echo "  docker[:repo]                 Generate Dockerfile for repository"
    echo "If no repository is passed, a command applies to all repositories in the working directory."
    exit 0
}

if [[ $# -eq 0 ]]; then
    COMMANDS=help
else
    COMMANDS=$@
fi

while getopts ":h" o; do
    case "${o}" in
    *) help ;;
    esac
done
shift $((OPTIND - 1))

if (($OPTIND == 1)); then
    for cmd in $COMMANDS; do
        $(echo $cmd | tr : ' ')
    done
fi
