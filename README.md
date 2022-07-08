# FeatJAR

This is the root project for FeatJAR.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

To get started on Ubuntu (via WSL on Windows), run:

```
# SETUP

# install dependencies (build-essential and libgmp required for sharpSAT)
sudo apt update
sudo apt install openjdk-11-jdk git maven ant build-essential libgmp-dev

# clone root project
git clone https://github.com/FeatJAR/FeatJAR.git # without push access
git clone git@github.com:FeatJAR/FeatJAR.git # with push access

# build default modules
cd FeatJAR
make
# if tests fail, try "make inst", which skips tests

# USAGE

# test whether a feature model is void
java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar analyze \
  -i cli/src/test/resources/testFeatureModels/car.xml -a void

# convert a feature model into CNF
java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar convert \
  -i cli/src/test/resources/testFeatureModels/car.xml -f dimacs -cnf -o car.dimacs

# convert a feature model and analyze it by means of pipes
cat cli/src/test/resources/testFeatureModels/car.xml | \
  java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar convert -f dimacs -cnf | \
  tail -n +2 | \
  java -jar cli/target/cli-1.0-SNAPSHOT-combined.jar analyze \
  -i "<stdin:dimacs>" -a cardinality
```
