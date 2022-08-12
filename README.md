# FeatJAR

This is the root project for FeatJAR.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## Build instructions

For developers, we recommend to run `git config --global url.ssh://git@github.com/.insteadOf https://github.com/` beforehand to push/pull repositories over SSH instead of HTTPS. 

### Ubuntu

Run the following in a shell:

```
sudo apt update
sudo apt install git openjdk-11-jdk
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
scripts/clone.bat
./gradlew build
```

Installation on other Linux distributions may differ slightly, depending on the package manager.

### Windows

Assuming [Chocolatey](https://chocolatey.org/install) is installed, run the following in `cmd.exe`: 

```
choco install git openjdk11
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
scripts\clone.bat
gradlew build
```

Alternatively, follow the steps for Ubuntu in a [WSL](https://docs.microsoft.com/en-us/windows/wsl/install) shell.
    
### macOS

Assuming [Homebrew](https://brew.sh/) is installed, run the following in a shell:

```
brew update
brew install git openjdk@11
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
scripts/clone.bat
./gradlew build
```

### Docker

Assuming [Git](https://git-scm.com/) and [Docker](https://docs.docker.com/get-docker/) are installed, run the following in a shell (or, on Windows, in WSL):

```
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
scripts/clone.bat
docker run -v "$(pwd)":/home/gradle gradle:7.5.1-jdk11 gradle build
```

To build a Docker image including FeatJAR (e.g., for reproducing evaluations), use `make docker`.

## Example usage

```
# test whether a feature model is void
java -jar cli/build/libs/cli-all.jar analyze -i cli/src/test/resources/testFeatureModels/car.xml -a void
  
# or, equivalently, using Gradle
gradlew :cli:run --args "analyze -i src/test/resources/testFeatureModels/car.xml -a void"

# convert a feature model into CNF
java -jar cli/build/libs/cli-all.jar convert \
  -i cli/src/test/resources/testFeatureModels/car.xml -f dimacs -cnf -o car.dimacs

# convert a feature model and analyze it by means of pipes
cat cli/src/test/resources/testFeatureModels/car.xml | \
  java -jar cli/build/libs/cli-all.jar convert -f dimacs -cnf | \
  tail -n +2 | \
  java -jar cli/build/libs/cli-all.jar analyze \
  -i "<stdin:dimacs>" -a cardinality
```

## Applications

In addition to the repositories under the [FeatJAR](https://github.com/FeatJAR) organization, several other tools and evaluations rely on FeatJAR, for example:

* [tseitin-or-not-tseitin](https://github.com/ekuiter/tseitin-or-not-tseitin): Automated evaluation of CNF transformations' impact on feature-model analyses
* [variED-NG](https://github.com/ekuiter/variED-NG): A collaborative, real-time feature model editor

## Contributors

FeatJAR development team:

* [Thomas Thüm](https://www.uni-ulm.de/in/sp/team/thuem/) (University of Ulm, Germany)
* [Sebastian Krieter](https://www.uni-ulm.de/in/sp/team/sebastian-krieter/) (University of Ulm, Germany)
* [Elias Kuiter](https://www.dbse.ovgu.de/Mitarbeiter/Elias+Kuiter.html) (University of Magdeburg, Germany)

Further contributors and former project members:

* Daniel Hohmann (University of Magdeburg, Germany)
