# FeatJAR

This is the root project for FeatJAR.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## Build instructions

Most modules run on the JVM and are therefore platform-independent.
However, the `native-*` modules (e.g., solvers) require a compilation step targeted to a specific platform.

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

## Example usage

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
