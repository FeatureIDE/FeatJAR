#! /bin/bash
WORKING_DIR=${PWD}

# Check if maven and ant are installed
PREREQUISITE_FAILED=0
if [ -z "$JAVA_HOME" ]; then
	echo 'Set JAVA_HOME first!'
	PREREQUISITE_FAILED=1
fi
ant -version 2>/dev/null || { echo 'Install Ant first!' ; PREREQUISITE_FAILED=1; } 
mvn -version 2>/dev/null || { echo 'Install Maven first!' ; PREREQUISITE_FAILED=1; } 
if [ "$PREREQUISITE_FAILED" -eq "1" ]; then
	echo 'Fail!'
	exit 1;
fi

# Build Maven Projects
function maven-install {
	cd $1
	echo ${PWD}
	mvn clean install
	if [[ "$?" -ne 0 ]] ; then
	  echo 'Error during build of lib '$1; exit $rc
	fi
	cd ${WORKING_DIR}
}

maven-install util
maven-install formula
maven-install formula-analysis
maven-install evaluation

maven-install evaluation-mig
maven-install evaluation-pc-sampling
maven-install evaluation-sampling-metrics

echo 'Success!'
