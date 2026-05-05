#! /bin/bash
git submodule update --init

cd d-dnnf-reasoner
cargo build --release --features d4
cargo build --release --bin dhone
