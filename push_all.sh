#! /bin/bash
WORKING_DIR=${PWD}

PREREQUISITE_FAILED=0
git --version 2>/dev/null || { echo 'Install Git first!' ; PREREQUISITE_FAILED=1; } 
if [ "$PREREQUISITE_FAILED" -eq "1" ]; then
	echo 'Fail!'
	exit 1;
fi

# Build Maven Projects
function git-push {
	cd $1
	echo 'Push '${PWD}
	git push
	if [[ "$?" -ne 0 ]] ; then
		echo 'Error during pushing of '$1
		git status
		exit -1
	fi
	cd ${WORKING_DIR}
}

if [ -z "$LOCAL_SSHKEY_SAVED" ]; then
	eval $(ssh-agent) && ssh-add && LOCAL_SSHKEY_SAVED=1 && export LOCAL_SSHKEY_SAVED;
fi

git-push .

git-push util
git-push formula
git-push formula-analysis
git-push formula-analysis-javasmt
git-push pc-extraction

git-push evaluation
git-push evaluation-mig
git-push evaluation-pc-sampling
git-push evaluation-sampling-metrics

echo 'Success!'
