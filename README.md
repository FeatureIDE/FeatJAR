# FeatJAR

This is the root project for FeatJAR.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## Build instructions

General remarks:

* Choose which modules to build by editing `modules.cfg` (copying `modules.template.cfg`, if necessary).
* Most modules run on the JVM and are therefore platform-independent.
  However, the `native-*` modules (e.g., solvers) require a compilation step targeted to a specific platform.
* For developers, we recommend to run `git config --global url.ssh://git@github.com/.insteadOf https://github.com/` beforehand to push/pull repositories over SSH instead of HTTPS. 

### Ubuntu

Run the following in a shell:

```
sudo apt update
sudo apt install openjdk-11-jdk git maven build-essential # required by all modules
sudo apt install libgmp-dev # required by the native-sharpsat module
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
make
```

Installation on other Linux distributions works analogously.

### Windows (WSL)

The recommended way to build on Windows is to use [WSL](https://docs.microsoft.com/en-us/windows/wsl/install).
After installing WSL, simply follow the instructions for Ubuntu in a `wsl` shell.
For most modules, the assembled JAR files do not differ to *Windows (MinGW)* below; however, for the `native-*` modules, Linux binaries will be compiled.
   
### Windows (MinGW)

If Windows binaries are required, the build process is a little more involved.

* As a prerequisite, install JDK 11, Git, and Maven, for example with [Chocolatey](https://chocolatey.org/install):
  ```
  choco install openjdk11 git maven
  ```
* To build the `native-sharpsat` module, also install [Visual Studio](https://visualstudio.microsoft.com/downloads/) with the C++ workload (the Windows SDK, in particular).
* Then, install [MinGW](https://sourceforge.net/projects/mingw/files/Installer/mingw-get-setup.exe/download) and add `C:\MinGW\bin` to the `Path` environment variable.
* Run the following in `cmd.exe` or PowerShell:
   ```
   mingw-get install mingw-developer-toolkit mingw32-base mingw32-gcc-g++ # required by all modules
   mingw-get install mingw32-gmp mingw32-libgmpxx # required by the native-sharpsat module
   ```
* Finally, run the following in an MSYS shell (`C:\MinGW\msys\1.0\msys.bat`):
   ```
   git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
   make
   ```
   
### macOS

`todo`

### Docker

To build FeatJAR inside a Docker container (compiling Linux binaries), install [Docker](https://docs.docker.com/get-docker/) on a 64-bit Linux 5.x system or [WSL 2](https://docs.microsoft.com/de-de/windows/wsl/install).
Then run the following in a shell:

```
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
docker compose run featjar
```

This does not make use of the local Maven repository, so creates a clean build on each `run`.
To build a Docker *image* including FeatJAR (e.g., for reproducing evaluations), have a look at TODO.

## Example usage

```
# test whether a feature model is void
java -jar cli/target/cli-1.0-SNAPSHOT-all.jar analyze \
  -i cli/src/test/resources/testFeatureModels/car.xml -a void

# convert a feature model into CNF
java -jar cli/target/cli-1.0-SNAPSHOT-all.jar convert \
  -i cli/src/test/resources/testFeatureModels/car.xml -f dimacs -cnf -o car.dimacs

# convert a feature model and analyze it by means of pipes
cat cli/src/test/resources/testFeatureModels/car.xml | \
  java -jar cli/target/cli-1.0-SNAPSHOT-all.jar convert -f dimacs -cnf | \
  tail -n +2 | \
  java -jar cli/target/cli-1.0-SNAPSHOT-all.jar analyze \
  -i "<stdin:dimacs>" -a cardinality
```

## Applications

In addition to the repositories under the [FeatJAR](https://github.com/FeatJAR) organization, several other tools and evaluations rely on FeatJAR, for example:

* [tseitin-or-not-tseitin](https://github.com/ekuiter/tseitin-or-not-tseitin): Automated evaluation of CNF transformations' impact on feature-model analyses
* [variED-NG](https://github.com/ekuiter/variED-NG): A collaborative, real-time feature model editor

## Contributors

FeatJAR development team:

* [Thomas Thüm](https://www.uni-ulm.de/in/sp/team/thuem/) (University of Ulm, Germany)
* [Sebastian Krieter](https://www.dbse.ovgu.de/Mitarbeiter/Externe+Doktoranden/Sebastian+Krieter.html) (Harz University of Applied Sciences, Germany)
* [Elias Kuiter](https://www.dbse.ovgu.de/Mitarbeiter/Elias+Kuiter.html) (University of Magdeburg, Germany)

Further contributors and former project members:

* Daniel Hohmann (University of Magdeburg, Germany)
