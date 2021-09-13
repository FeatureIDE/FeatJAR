#! /bin/bash

REPOS=(. util formula formula-analysis formula-analysis-javasmt pc-extraction FeatureCoPP evaluation evaluation-mig evaluation-pc-sampling evaluation-tseytin) # todo: add evaluation-sampling-metrics
WORKING_DIR=${PWD}

setup() {
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

	# Add SSH key
	if [ -z "$LOCAL_SSHKEY_SAVED" ]; then
		eval $(ssh-agent) && ssh-add && LOCAL_SSHKEY_SAVED=1 && export LOCAL_SSHKEY_SAVED;
	fi
}

# Pull projects from GitHub
pull() {
	if [ -d "$1" ]; then
		cd $1
		echo 'Pulling in '${PWD}
		git pull
		if [[ "$?" -ne 0 ]] ; then
			>&2 echo
			>&2 echo 'ERROR during pulling of '$1
			>&2 echo
			git status
		fi
		cd ${WORKING_DIR}
	else
		# try to clone with SSH, alternatively with HTTPS
		echo 'Cloning in '${PWD}
		git clone git@github.com:skrieter/$1.git
		if [[ "$?" -ne 0 ]] ; then
			git clone https://github.com/skrieter/$1.git
			if [[ "$?" -ne 0 ]] ; then
				>&2 echo 'Error during cloning of '$1; exit -1
			fi
		fi
	fi
}

# Push projects to GitHub
push() {
	if [ -d "$1" ]; then
		cd $1
		echo 'Pushing in '${PWD}
		git push
		if [[ "$?" -ne 0 ]] ; then
			>&2 echo
			>&2 echo 'ERROR during pushing of '$1
			>&2 echo
			git status
		fi
		cd ${WORKING_DIR}
	else
		>&2 echo 'Repo '$1' does not exist'
	fi
}

update-all() {
	setup
	for repo in ${REPOS[@]}; do pull $repo; done
}

push-all() {
	setup
	for repo in ${REPOS[@]}; do push $repo; done
}

build-all() {
	update
	mvn clean install
}

usage() { echo "Usage: $0 [-b] [-u] [-p]" 1>&2; exit 1; }

while getopts ":bupc" o; do
    case "${o}" in
		c)
			mvn clean
			;;
		b)
			build-all
			;;
        u)
			update-all
            ;;
        p)
			push-all
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if (( $OPTIND == 1 )); then
   build-all
fi
