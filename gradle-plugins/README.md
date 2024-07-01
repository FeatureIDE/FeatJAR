# FeatJAR-gradle-plugins

This repository contains build logic shared by several FeatJAR modules, implemented as Gradle plugins.

We provide the following plugins, which can be used in `build.gradle` files:

* `de.featjar.java`: Convention plugin for Java modules (takes care of formatting, license headers, releasing, ...)
* `de.featjar.java-library`: Extends `de.featjar.java` for developing libraries
* `de.featjar.java-application`: Extends `de.featjar.java` for developing applications (creates shadow JARs)

## License

This repository belongs to [FeatJAR](https://github.com/FeatureIDE/FeatJAR), a collection of Java libraries for feature-oriented software development.
FeatJAR is released under the GNU Lesser General Public License v3.0.