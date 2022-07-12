# FeatJAR

This is the root project for FeatJAR.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## Build on Ubuntu

```
# install dependencies
sudo apt update
sudo apt install openjdk-11-jdk git maven ant build-essential libgmp-dev

# clone root project
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR

# build default modules (run "make help" for more information)
make 
```

## Build on Windows

```
...
```

## Usage

```
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
