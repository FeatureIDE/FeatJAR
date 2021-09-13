#! /bin/bash
# To control the built repos, (un)comment them in pom.xml under <modules>.

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

update-all() {
    git pull --recurse-submodules -j8
}

push-all() {
    git submodule foreach 'git push origin master || true'
    git push origin master
}

build-all() {
    update-all
    mvn clean install
}

usage() { echo "Usage: $0 [-b] [-u] [-p] [-c] [-r]" 1>&2; exit 1; }

while getopts ":bupcr" o; do
    case "${o}" in
        r) mvn clean ;;
        c) mvn install ;;
        b) build-all ;;
        u) update-all ;;
        p) push-all ;;
        *) usage ;;
    esac
done
shift $((OPTIND-1))

if (( $OPTIND == 1 )); then
   build-all
fi
