# FeatJAR

This is the root project for **FeatJAR**, a collection of Java libraries for feature-oriented software development, planned to replace the [FeatureIDE library](https://featureide.github.io/#download), starting with FeatureIDE 4.0.0.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## How to Build

For developers who intend to push changes, we recommend to run `git config --global url.ssh://git@github.com/.insteadOf https://github.com/` (do not forget the trailing slash) beforehand to push/pull repositories over SSH instead of HTTPS.

### On Ubuntu

Run the following in a shell:

```
sudo apt update
sudo apt install git openjdk-11-jdk
git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
scripts/clone.bat
./gradlew build
```

Installation on other Linux distributions may differ slightly, depending on the package manager.

### On Windows

Assuming [Chocolatey](https://chocolatey.org/install) is installed, run the following in `cmd.exe`: 

```
choco install git openjdk11
git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
scripts\clone.bat
gradlew build
```

Alternatively, follow the steps for Ubuntu in a [WSL](https://docs.microsoft.com/en-us/windows/wsl/install) shell.
    
### On macOS

Assuming [Homebrew](https://brew.sh/) is installed, run the following in a shell:

```
brew update
brew install git openjdk@11
git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
scripts/clone.bat
./gradlew build
```

### Using Docker

Assuming [Git](https://git-scm.com/) and [Docker](https://docs.docker.com/get-docker/) are installed, run the following in a shell (or, on Windows, in WSL):

```
git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
scripts/clone.bat
docker run -v "$(pwd)":/home/gradle gradle:8.0.2-jdk11 gradle build
```

### More about Gradle

Besides building and testing all modules, Gradle supports many commands to control the build process, including:

```
# assemble all modules as JARs and run all tests (= assemble + check)
./gradlew build

# assemble all modules as JARs, skipping all tests
./gradlew assemble

# run all tests for all modules, skipping JAR assembly
./gradlew check

# run the FeatJAR command-line interface with the specified arguments
./gradlew :cli:run --args "<arguments>"

# run a task (e.g., 'build') only for the specified module (e.g., 'base') and its dependencies
./gradlew :<module>:<task>

# print a comprehensive list of tasks 
gradle :<module>:tasks
```

## How to Use

### As an Executable

```
# count feature model solutions
java -jar cli/build/libs/cli-*-all.jar --command countsharpsat --input cli/src/test/resources/testFeatureModels/car.xml
  
# or, equivalently, using Gradle
./gradlew :cli:run --args " --command countsharpsat --input src/test/resources/testFeatureModels/car.xml"
```

### As a Library

In addition to the repositories under the [FeatureIDE](https://github.com/FeatureIDE) organization, several other tools and evaluations rely on FeatJAR as a library, for example:

* [tseitin-or-not-tseitin](https://github.com/ekuiter/tseitin-or-not-tseitin): Automated evaluation of CNF transformations' impact on feature-model analyses
* [variED-NG](https://github.com/ekuiter/variED-NG): A collaborative, real-time feature model editor

### In FeatureIDE

To use FeatJAR during FeatureIDE development, import the FeatJAR root folder as a Gradle project with default settings in Eclipse (`File > Import... > Gradle > Existing Gradle project`).
Then you can use any FeatJAR repository (e.g., `util`) in any Eclipse project by adding it to the project's build path (`Right click project > Properties > Java Build Path > Projects > Add...`).

## Contributors

FeatJAR development team:

* [Thomas Thüm](https://www.uni-ulm.de/in/sp/team/thuem/) (University of Ulm, Germany)
* [Sebastian Krieter](https://www.uni-ulm.de/in/sp/team/sebastian-krieter/) (University of Ulm, Germany)
* [Elias Kuiter](https://www.dbse.ovgu.de/Mitarbeiter/Elias+Kuiter.html) (University of Magdeburg, Germany)

Further contributors and former project members:

* Katjana Herbst (University of Ulm, Germany)
* Daniel Hohmann (University of Magdeburg, Germany)
* Timo Zuccarello (University of Ulm, Germany)