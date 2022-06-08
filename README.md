## spldev root project

### Getting started (Ubuntu)

```
# install dependencies (libgmp required for sharpSAT)
sudo apt update
sudo apt install git default-jre maven ant libgmp-dev

# without push access
git clone https://github.com/skrieter/spldev.git
# with push access
git clone git@github.com:skrieter/spldev.git

cd spldev
./build.sh
```
