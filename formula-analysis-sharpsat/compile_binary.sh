#! /bin/bash
git submodule update --init

cd sharpSAT
cmake . && make && cp sharpSAT ../src/main/resources/bin/
