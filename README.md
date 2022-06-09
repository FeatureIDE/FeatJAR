## spldev root project

This is the root project for the new FeatureIDE architecture, report feedback to sebastian.krieter@ovgu.de.

To get started on Ubuntu or WSL, run:

```
# SETUP

# install dependencies (libgmp required for sharpSAT)
sudo apt update
sudo apt install openjdk-11-jdk maven ant libgmp-dev
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# clone root project
git clone https://github.com/skrieter/spldev.git # without push access
git clone git@github.com:skrieter/spldev.git # with push access

# clone and build default modules
cd spldev
./build.sh

# USAGE

# test whether a feature model is void
java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar analyze -i cli/src/test/resources/testFeatureModels/car.xml -a void

# convert a feature model into CNF
java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar convert -i cli/src/test/resources/testFeatureModels/car.xml -f dimacs -cnf -o car.dimacs

# convert a feature model and analyze it by means of pipes
cat cli/src/test/resources/testFeatureModels/car.xml | java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar convert -f dimacs -cnf | tail -n +2 | java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar analyze -i "<stdin:dimacs>" -a cardinality
```
