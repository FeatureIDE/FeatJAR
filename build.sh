#! /bin/bash
# To control the built/pushed/pulled modules, (un)comment them in build.cfg.
MODULES="$(cat build.cfg | grep -E "^[^#]")"

pushd() {
    command pushd "$@" > /dev/null
}

popd() {
    command popd "$@" > /dev/null
}

# Pull projects from GitHub
pull() {
    if [ -d $1/.git ]; then
        pushd $1
        echo 'Pulling '$1
        git pull origin master -q
        if [[ "$?" -ne 0 ]] ; then
            >&2 echo
            >&2 echo 'ERROR during pulling of '$1
            >&2 echo
            git status
        fi
        popd
    else
        # try to clone with SSH, alternatively with HTTPS
        echo 'Cloning '$1
        git clone --recurse-submodules -j8 git@github.com:skrieter/$1.git -q
        if [[ "$?" -ne 0 ]] ; then
            git clone https://github.com/skrieter/$1.git -q
            if [[ "$?" -ne 0 ]] ; then
                >&2 echo 'Error during cloning of '$1; exit -1
            fi
        fi
    fi
}

# Push projects to GitHub
push() {
    if [ -d $1/.git ]; then
        pushd $1
        echo 'Pushing '$1
        git push origin master -q
        if [[ "$?" -ne 0 ]] ; then
            >&2 echo
            >&2 echo 'ERROR during pushing of '$1
            >&2 echo
            git status
        fi
        popd
    else
        >&2 echo 'Repo '$1' does not exist'
    fi
}

# Check if maven and ant are installed
PREREQUISITE_FAILED=0
if [ -z "$JAVA_HOME" ]; then
    >&2 echo 'Set JAVA_HOME first!'
    PREREQUISITE_FAILED=1
fi
git --version 1>/dev/null 2>/dev/null || { >&2 echo 'Install Git first!' ; PREREQUISITE_FAILED=1; } 
mvn --version 1>/dev/null 2>/dev/null || { >&2 echo 'Install Maven first!' ; PREREQUISITE_FAILED=1; } 
ant -version 1>/dev/null 2>/dev/null || { >&2 echo 'Install Ant first!' ; PREREQUISITE_FAILED=1; } 
if [ "$PREREQUISITE_FAILED" -eq "1" ]; then
    >&2 echo 'Fail!'
    exit 1;
fi

add-ssh-key() {
    if [ -z "$LOCAL_SSHKEY_SAVED" ] && ssh-agent --version 1>/dev/null 2>/dev/null; then
        eval $(ssh-agent) && ssh-add && LOCAL_SSHKEY_SAVED=1 && export LOCAL_SSHKEY_SAVED;
    fi
}

pull-all() {
    add-ssh-key
    for module in $MODULES; do
        pull $module
    done
    echo "Pulling root"
    git pull origin master -q
}

push-all() {
    add-ssh-key
    for module in $MODULES; do
        push $module
    done
    echo "Pushing root"
    git push origin master -q
}

compile-all() {
    sed -i -E "s#<!--<module>(.*?)</module>-->#<module>\1</module>#" pom.xml
    sed -i -E "s#<module>(.*?)</module>#<!--<module>\1</module>-->#" pom.xml
    for module in $MODULES; do
        sed -i "s#<!--<module>$module</module>-->#<module>$module</module>#" pom.xml
    done
    mvn clean install
}

build-all() {
    pull-all
    compile-all
}

usage() { echo "Usage: $0 [-b] [-u] [-p] [-c] [-r]" 1>&2; exit 1; }

while getopts ":bupcr" o; do
    case "${o}" in
        c) compile-all ;;
        b) build-all ;;
        u) pull-all ;;
        p) push-all ;;
        *) usage ;;
    esac
done
shift $((OPTIND-1))

if (( $OPTIND == 1 )); then
   build-all
fi
