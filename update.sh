#! /bin/bash
WORKING_DIR=${PWD}

# Check if maven and ant are installed
PREREQUISITE_FAILED=0
if [ -z "$JAVA_HOME" ]; then
	echo 'Set JAVA_HOME first!'
	PREREQUISITE_FAILED=1
fi
git --version 2>/dev/null || { echo 'Install Git first!' ; PREREQUISITE_FAILED=1; } 
if [ "$PREREQUISITE_FAILED" -eq "1" ]; then
	echo 'Fail!'
	exit 1;
fi

# Build Maven Projects
function git-update {
	if [ -d "$1" ]; then
		cd $1
		echo 'Pulling in '${PWD}
		git pull -r
		if [[ "$?" -ne 0 ]] ; then
			echo 'Error during pulling of '$1
			git status
			exit -1
		fi
		cd ${WORKING_DIR}
	else
		echo 'Cloning in '${PWD}
		git clone git@github.com:skrieter/$1.git
		if [[ "$?" -ne 0 ]] ; then
			echo 'Error during cloning of '$1; exit -1
		fi
	fi
}

if [ -z "$LOCAL_SSHKEY_SAVED" ]; then
	eval $(ssh-agent) && ssh-add && LOCAL_SSHKEY_SAVED=1 && export LOCAL_SSHKEY_SAVED;
fi

git-update util
git-update formula
git-update formula-analysis
git-update evaluation

git-update evaluation-mig
git-update evaluation-pc-sampling
git-update evaluation-sampling-metrics

echo 'Success!'
