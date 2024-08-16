# FeatJAR

This is the root project for **FeatJAR**, a collection of Java libraries for feature-oriented software development, planned to replace the [FeatureIDE library](https://featureide.github.io/#download), starting with FeatureIDE 4.0.0.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## How to build

For developers who intend to push changes, we recommend to run `git config --global url.ssh://git@github.com/.insteadOf https://github.com/` (do not forget the trailing slash) beforehand to push/pull repositories over SSH instead of HTTPS.


FeatJAR is still in active development. Thus, there can be test fails in some projects. Run `gradle assemble` instead of `gradle build` to skip tests.

### On Ubuntu

Run the following in a shell:

    sudo apt update
    sudo apt install git openjdk-11-jdk
    git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
    scripts/clone.bat
    ./gradlew build

Installation on other Linux distributions may differ slightly, depending on the package manager.

### On Windows

Assuming [Chocolatey](https://chocolatey.org/install) is installed, run the following in `cmd.exe`: 

    choco install git openjdk11
    git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
    scripts\clone.bat
    gradlew build

Alternatively, follow the steps for Ubuntu in a [WSL](https://docs.microsoft.com/en-us/windows/wsl/install) shell.
    
### On macOS

Assuming [Homebrew](https://brew.sh/) is installed, run the following in a shell:

    brew update
    brew install git openjdk@11
    git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
    scripts/clone.bat
    ./gradlew build

### Using Docker

Assuming [Git](https://git-scm.com/) and [Docker](https://docs.docker.com/get-docker/) are installed, run the following in a shell (or, on Windows, in WSL):

    git clone https://github.com/FeatureIDE/FeatJAR.git && cd FeatJAR
    scripts/clone.bat
    docker run -v "$(pwd)":/home/gradle gradle:8.0.2-jdk11 gradle build

### More about Gradle

Besides building and testing all modules, Gradle supports many commands to control the build process, including:

    # assemble all modules as JARs and run all tests (= assemble + check)
    ./gradlew build
    
    # assemble all modules as JARs, skipping all tests
    ./gradlew assemble
    
    # run all tests for all modules, skipping JAR assembly
    ./gradlew check
    
    # run the FeatJAR command-line interface with the specified arguments
    ./gradlew :cli:run --args "<arguments>"
    
    # run a task (e.g., 'build') only for the specified module (e.g., 'base') and its dependencies
    ./gradlew :<module>:<task>
    
    # print a comprehensive list of tasks 
    gradle :<module>:tasks

## How to run

### Run as an Executable

    # count feature model solutions
    java -jar all/build/libs/*-all.jar count-sharpsat --input formula/src/testFixtures/resources/testFeatureModels/car.xml
      
    # or, equivalently, using Gradle
    ./gradlew :all:run --args " count-sharpsat --input ../formula/src/testFixtures/resources/testFeatureModels/car.xml"

### Run as a Library

In addition to the repositories under the [FeatureIDE](https://github.com/FeatureIDE) organization, several other tools and evaluations rely on FeatJAR as a library, for example:

* [tseitin-or-not-tseitin](https://github.com/ekuiter/tseitin-or-not-tseitin): Automated evaluation of CNF transformations' impact on feature-model analyses
* [variED-NG](https://github.com/ekuiter/variED-NG): A collaborative, real-time feature model editor

### Run in FeatureIDE

To use FeatJAR during FeatureIDE development, import the FeatJAR root folder as a Gradle project with default settings in Eclipse (`File > Import... > Gradle > Existing Gradle project`).
Then you can use any FeatJAR repository (e.g., `base`) in any Eclipse project by adding it to the project's build path (`Right click project > Properties > Java Build Path > Projects > Add...`).

## How to execute commands

### Using the `--help` flag

You can get an overview of all commands by appending the --help flag.
For example, when using the `--help` flag on the *formula-analysis-sat4j* package, the following output will be produced:

    $ java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar --help

    Usage: java -jar feat.jar [<command> | --command <classpath>] [--<flag> | --<option> <value>]...

    General options:
            --config <value1,value2,...>: The names of configuration files (default: [])
            --config_dir <value>: The path to the configuration files
            --command <value>: Classpath from command to execute
            --help: Print usage information
            --version: Print version information
            --print-stacktrace: Print a stacktrace for all logged exceptions
            --quiet: Suppress all unnecessary output. (Overwrites --log-info and --log-error options)
            --info-file <value>: Path to info log file
            --error-file <value>: Path to error log file
            --log-info <value1,value2,...>: Message types printed to the info stream (message, error, warning, info, debug, progress) (default: [MESSAGE, INFO, PROGRESS])
            --log-error <value1,value2,...>: Message types printed to the error stream (message, error, warning, info, debug, progress) (default: [ERROR])
            --log-info-file <value1,value2,...>: Message types printed to the info file (message, error, warning, info, debug, progress) (default: [MESSAGE, INFO, DEBUG])
            --log-error-file <value1,value2,...>: Message types printed to the error file (message, error, warning, info, debug, progress) (default: [ERROR, WARNING])
    
    The following commands are available:
            atomic-sets-sat4j: Computes atomic sets for a given formula using SAT4J.
                    (Classpath: de.featjar.analysis.sat4j.cli.AtomicSetsCommand)
            convert-cnf-format: Converts the format of a given formula into another CNF format.
                    (Classpath: de.featjar.formula.cli.ConvertCNFFormatCommand)
            convert-format: Converts the format of a given formula.
                    (Classpath: de.featjar.formula.cli.ConvertFormatCommand)
            core-sat4j: Computes core and dead variables for a given formula using SAT4J.
                    (Classpath: de.featjar.analysis.sat4j.cli.CoreCommand)
            count-sat4j: Computes number of solutions for a given formula using SAT4J.
                    (Classpath: de.featjar.analysis.sat4j.cli.SolutionCountCommand)
            print: Prints the formula in a readable format.
                    (Classpath: de.featjar.formula.cli.PrintCommand)
            projection-sat4j: Removes literals of a given formula using SAT4J.
                    (Classpath: de.featjar.analysis.sat4j.cli.ProjectionCommand)
            solutions-sat4j: Computes solutions for a given formula using SAT4J.
                    (Classpath: de.featjar.analysis.sat4j.cli.SolutionsCommand)
            t-wise-sat4j: Computes solutions for a given formula using SAT4J
                    (Classpath: de.featjar.analysis.sat4j.cli.TWiseCommand)



If you want more details of one specific command, you can again use the `--help` flag.
Here is an example:

    $ java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar print --help
    General options:
            --config <value1,value2,...>: The names of configuration files (default: [])
            --config_dir <value>: The path to the configuration files
            --command <value>: Classpath from command to execute
            --help: Print usage information
            --version: Print version information
            --print-stacktrace: Print a stacktrace for all logged exceptions
            --quiet: Suppress all unnecessary output. (Overwrites --log-info and --log-error options)
            --info-file <value>: Path to info log file
            --error-file <value>: Path to error log file
            --log-info <value1,value2,...>: Message types printed to the info stream (message, error, warning, info, debug, progress) (default: [MESSAGE, INFO, PROGRESS])
            --log-error <value1,value2,...>: Message types printed to the error stream (message, error, warning, info, debug, progress) (default: [ERROR])
            --log-info-file <value1,value2,...>: Message types printed to the info file (message, error, warning, info, debug, progress) (default: [MESSAGE, INFO, DEBUG])
            --log-error-file <value1,value2,...>: Message types printed to the error file (message, error, warning, info, debug, progress) (default: [ERROR, WARNING])

    Help for de.featjar.formula.cli.PrintCommand
            Prints the formula in a readable format.

            Options of command de.featjar.formula.cli.PrintCommand:
                    --enforce-parentheses: Enforces parentheses.
                    --enquote-whitespace: Enquotes whitespace.
                    --format <value>: Defines the symbols. (default: de.featjar.formula.io.textual.ShortSymbols: [not, and, or, implies, biimplies, choose, atleast, between, atmost, exists, forall, -, &, |, =>, <=>])
                    --input <value>: Path to input file(s)
                    --newline <value>: Defines the new line value. Possible options: [TAB, NEWLINE, SPACE]. For custom value, type CUSTOM:<value> (default: NEWLINE)
                    --notation <value>: Defines the notation. Possible options: [INFIX, PREFIX, POSTFIX, TREE] (default: INFIX)
                    --output <value>: Path to output file(s)
                    --tab <value>: Defines the tab value. Possible options: [TAB, NEWLINE, SPACE]. For custom value, type CUSTOM:<value> (default: TAB)

### Using the `--command` option

If several libraries are imported that contain commands with the same name, FeatJAR does not know which command should be executed.
Therefore, it is possible to specify the classpath of the command class with the `--command` option.
You can get a list of all classpaths by using the `--help` flag as shown above.
For example, when executing the `print-sat4j` command, you could instead type:

    java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar --command de.featjar.formula.visitor.cli.PrintCommand ...

### Full example of the `print` command

Every option and flag is specified in this example. You can get an overview of all options by using the `--help` flag as shown above.

     java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar print --input formula/src/testFixtures/resources/GPL/model.xml --notation PREFIX --format de.featjar.formula.io.textual.JavaSymbols --enforce-parentheses --enquote-whitespace

The output will be printed in the console because no `--output` option was set:

    &&(||(GPL) ||(!MainGpl GPL) ||(!HiddenGtp MainGpl) ||(!MainGpl HiddenGtp) ||(!DirectedWithEdges HiddenGtp) ||(!DirectedWithNeighbors HiddenGtp) ||(!DirectedOnlyVertices HiddenGtp) ||(!UndirectedWithEdges HiddenGtp) ||(!UndirectedWithNeighbors HiddenGtp) ||(!UndirectedOnlyVertices HiddenGtp) ||(!HiddenGtp DirectedWithEdges DirectedWithNeighbors DirectedOnlyVertices UndirectedWithEdges UndirectedWithNeighbors UndirectedOnlyVertices) ||(!DirectedWithEdges !DirectedWithNeighbors) ||(!DirectedWithEdges !DirectedOnlyVertices) ||(!DirectedWithEdges !UndirectedWithEdges) ||(!DirectedWithEdges !UndirectedWithNeighbors) ||(!DirectedWithEdges !UndirectedOnlyVertices) ||(!DirectedWithNeighbors !DirectedOnlyVertices) ||(!DirectedWithNeighbors !UndirectedWithEdges) ||(!DirectedWithNeighbors !UndirectedWithNeighbors) ||(!DirectedWithNeighbors !UndirectedOnlyVertices) ||(!DirectedOnlyVertices !UndirectedWithEdges) ||(!DirectedOnlyVertices !UndirectedWithNeighbors) ||(!DirectedOnlyVertices !UndirectedOnlyVertices) ||(!UndirectedWithEdges !UndirectedWithNeighbors) ||(!UndirectedWithEdges !UndirectedOnlyVertices) ||(!UndirectedWithNeighbors !UndirectedOnlyVertices) ||(!TestProg MainGpl) ||(!MainGpl TestProg) ||(!Alg MainGpl) ||(!MainGpl Alg) ||(!Number Alg) ||(!Connected Alg) ||(!StrongC Alg) ||(!StronglyConnected StrongC) ||(!StrongC StronglyConnected) ||(!Transpose StrongC) ||(!StrongC Transpose) ||(!Cycle Alg) ||(!MSTPrim Alg) ||(!MSTKruskal Alg) ||(!Alg Number Connected StrongC Cycle MSTPrim MSTKruskal) ||(!Src MainGpl) ||(!MainGpl Src) ||(!BFS Src) ||(!DFS Src) ||(!Src BFS DFS) ||(!BFS !DFS) ||(!HiddenWgt MainGpl) ||(!MainGpl HiddenWgt) ||(!WeightOptions HiddenWgt) ||(!HiddenWgt WeightOptions) ||(!WeightedWithEdges WeightOptions) ||(!WeightedWithNeighbors WeightOptions) ||(!WeightedOnlyVertices WeightOptions) ||(!Wgt MainGpl) ||(!MainGpl Wgt) ||(!Weighted Wgt) ||(!Unweighted Wgt) ||(!Wgt Weighted Unweighted) ||(!Weighted !Unweighted) ||(!Gtp MainGpl) ||(!MainGpl Gtp) ||(!Directed Gtp) ||(!Undirected Gtp) ||(!Gtp Directed Undirected) ||(!Directed !Undirected) ||(!Implementation MainGpl) ||(!MainGpl Implementation) ||(!OnlyVertices Implementation) ||(!WithNeighbors Implementation) ||(!WithEdges Implementation) ||(!Implementation OnlyVertices WithNeighbors WithEdges) ||(!OnlyVertices !WithNeighbors) ||(!OnlyVertices !WithEdges) ||(!WithNeighbors !WithEdges) ||(!Base MainGpl) ||(!MainGpl Base) ||(!GPL MainGpl) ||(!Number Src) ||(!Number Gtp) ||(!Connected Src) ||(!Connected Undirected) ||(!StrongC DFS) ||(!StrongC Directed) ||(!Cycle DFS) ||(!Cycle Gtp) ||(Weighted !MSTPrim) ||(Weighted !MSTKruskal) ||(Undirected !MSTPrim) ||(Undirected !MSTKruskal) ||(!MSTPrim !MSTKruskal) ||(!MSTKruskal WithEdges) ||(!OnlyVertices !Weighted WeightedOnlyVertices) ||(!WeightedOnlyVertices Weighted) ||(!WeightedOnlyVertices OnlyVertices) ||(!WithNeighbors !Weighted WeightedWithNeighbors) ||(!WeightedWithNeighbors Weighted) ||(!WeightedWithNeighbors WithNeighbors) ||(!WithEdges !Weighted WeightedWithEdges) ||(!WeightedWithEdges Weighted) ||(!WeightedWithEdges WithEdges) ||(!OnlyVertices !Directed DirectedOnlyVertices) ||(!DirectedOnlyVertices Directed) ||(!DirectedOnlyVertices OnlyVertices) ||(!WithNeighbors !Directed DirectedWithNeighbors) ||(!DirectedWithNeighbors Directed) ||(!DirectedWithNeighbors WithNeighbors) ||(!WithEdges !Directed DirectedWithEdges) ||(!DirectedWithEdges Directed) ||(!DirectedWithEdges WithEdges) ||(!OnlyVertices !Undirected UndirectedOnlyVertices) ||(!UndirectedOnlyVertices Undirected) ||(!UndirectedOnlyVertices OnlyVertices) ||(!WithNeighbors !Undirected UndirectedWithNeighbors) ||(!UndirectedWithNeighbors Undirected) ||(!UndirectedWithNeighbors WithNeighbors) ||(!WithEdges !Undirected UndirectedWithEdges) ||(!UndirectedWithEdges Undirected) ||(!UndirectedWithEdges WithEdges))

## Contributors

FeatJAR development team:

* [Sebastian Krieter](https://www.uni-ulm.de/in/sp/team/sebastian-krieter/) (Paderborn University, Germany)
* [Elias Kuiter](https://www.dbse.ovgu.de/Mitarbeiter/Elias+Kuiter.html) (University of Magdeburg, Germany)
* [Andreas Gerasimow](https://www.andreasgera.de) (Ulm University, Germany)
* [Thomas Thüm](https://www.uni-ulm.de/in/sp/team/thuem/) (Paderborn University, Germany)

Further contributors and former project members:

* Katjana Herbst (Ulm University, Germany)
* Daniel Hohmann (University of Magdeburg, Germany)
* Timo Zuccarello (Ulm University, Germany)
