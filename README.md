# FeatJAR

This is the root project for FeatJAR.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## Build instructions

Most modules run on the JVM and are therefore platform-independent.
However, the `native-*` modules (e.g., solvers) require a compilation step targeted to a specific platform.

### Ubuntu

Run the following in a shell.

```
sudo apt update
sudo apt install openjdk-11-jdk git maven ant build-essential libgmp-dev
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
make
```

### Windows

As a prerequisite, install JDK 11, Git, Maven, and Ant.
Then, install [MinGW](https://sourceforge.net/projects/mingw/files/Installer/mingw-get-setup.exe/download) and add `C:\MinGW\bin` to your `Path` variable.
Run the following in an MSYS shell (`C:\MinGW\msys\1.0\msys.bat`).

```
mingw-get install mingw-developer-toolkit mingw32-base mingw32-gcc-g++ mingw32-gmp mingw32-gmp-dev mingw32-libgmpxx
git clone https://github.com/FeatJAR/FeatJAR.git && cd FeatJAR
make
```

### Cross-compiling

To compile Linux binaries on Windows using [WSL](https://docs.microsoft.com/en-us/windows/wsl/install), simply follow the instructions for Ubuntu in a `wsl` shell.

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
