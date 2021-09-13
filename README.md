## spldev root project

### Getting started

Assumes Bash, Git, Maven, and Ant to be installed.

```
# without push access
git clone --recurse-submodules -j8 https://github.com/skrieter/spldev.git
# with push access
git clone --recurse-submodules -j8 git@github.com:skrieter/spldev.git
cd spldev
# make sure the submodules are not in a detached state, so new commits are made on master branch
git submodule foreach -q --recursive git checkout master
./build.sh
```

You can add/remove built projects in pom.xml.