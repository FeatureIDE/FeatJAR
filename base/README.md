# FeatJAR-base

This repository contains common utilities and data structures shared by several FeatJAR modules.

Some functionality included in this module:

* registration of native dependencies
* utilities for implementing command line interfaces
* helpers for data caching and error handling
* management of extensions and extension points
* input/output for common file formats
* job monitoring and execution
* logging facilities
* tree data structures and algorithms

## TODOs

Besides `TODO` items in the source code, the following general `TODOs` apply:

* The use of unchecked exceptions (e.g., `RuntimeException`) should be minimized.
  Either throw checked exceptions or (better) use `Optional` or `Result`.

## License

This repository belongs to [FeatJAR](https://github.com/FeatureIDE/FeatJAR), a collection of Java libraries for feature-oriented software development.
FeatJAR is released under the GNU Lesser General Public License v3.0.