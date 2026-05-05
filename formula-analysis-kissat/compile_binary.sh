#! /bin/bash
git submodule update --init

cd kissat
./configure && make && cp build/kissat ../src/main/resources/bin/
