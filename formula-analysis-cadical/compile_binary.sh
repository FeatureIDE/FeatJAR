#! /bin/bash
git submodule update --init

cd cadical
./configure && make && cp build/cadical ../src/main/resources/bin/
cd ..

cd cadiback
./configure && make && cp cadiback ../src/main/resources/bin/
