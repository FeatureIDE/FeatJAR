## spldev root project

### Getting started (Ubuntu/WSL)

```
# install dependencies (libgmp required for sharpSAT)
sudo apt update
sudo apt install openjdk-11-jdk maven ant libgmp-dev
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# without push access
git clone https://github.com/skrieter/spldev.git
# with push access
git clone git@github.com:skrieter/spldev.git

cd spldev
./build.sh
```
