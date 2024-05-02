# FeatJAR

This is the root project for **FeatJAR**, a collection of Java libraries for feature-oriented software development, planned to replace the [FeatureIDE library](https://featureide.github.io/#download), starting with FeatureIDE 4.0.0.
Please report feedback to sebastian.krieter@uni-ulm.de or kuiter@ovgu.de.

## How to Build

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

## How to Use

### Run as an Executable

    # count feature model solutions
    java -jar cli/build/libs/cli-*-all.jar --command countsharpsat --input cli/src/test/resources/testFeatureModels/car.xml
      
    # or, equivalently, using Gradle
    ./gradlew :cli:run --args " --command countsharpsat --input src/test/resources/testFeatureModels/car.xml"

### Run as a Library

In addition to the repositories under the [FeatureIDE](https://github.com/FeatureIDE) organization, several other tools and evaluations rely on FeatJAR as a library, for example:

* [tseitin-or-not-tseitin](https://github.com/ekuiter/tseitin-or-not-tseitin): Automated evaluation of CNF transformations' impact on feature-model analyses
* [variED-NG](https://github.com/ekuiter/variED-NG): A collaborative, real-time feature model editor

### Run in FeatureIDE

To use FeatJAR during FeatureIDE development, import the FeatJAR root folder as a Gradle project with default settings in Eclipse (`File > Import... > Gradle > Existing Gradle project`).
Then you can use any FeatJAR repository (e.g., `util`) in any Eclipse project by adding it to the project's build path (`Right click project > Properties > Java Build Path > Projects > Add...`).


### Using the `--help` flag
You can get an overview of all commands by appending the --help flag.
Here is an example with an executable:

    java -jar cli/build/libs/cli-*-all.jar --help

When using the `--help` flag on the *formula-analysis-sat4j* package, the following output will be produced:

    $ java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar --help
    Usage: java -jar feat.jar [--command <command> | <shortcut>] [--<flag> | --<option> <value>]...

    The following commands are available:
            de.featjar.formula.analysis.cli.CoreCommand: Computes core and dead variables for a given formula using SAT4J.
            de.featjar.formula.analysis.cli.AtomicSetsCommand: Computes atomic sets for a given formula using SAT4J.
            de.featjar.formula.analysis.cli.SolutionsCommand: Computes solutions for a given formula using SAT4J.
            de.featjar.formula.analysis.cli.SolutionCountCommand: Computes number of solutions for a given formula using SAT4J.
            de.featjar.formula.analysis.cli.TWiseCommand: Computes solutions for a given formula using SAT4J
            de.featjar.formula.transform.cli.ProjectionCommand: Removes literals of a given formula using SAT4J.
            de.featjar.formula.transform.cli.PrintCommand: Prints the formula in a readable format using SAT4J.
            de.featjar.formula.transform.cli.ConvertCNFFormatCommand: Converts the format of a given formula into another CNF format using SAT4J.
            de.featjar.formula.transform.cli.ConvertFormatCommand: Converts the format of a given formula using SAT4J.
    
    The following shortcuts are available:
            core-sat4j: de.featjar.formula.analysis.cli.CoreCommand
            atomic-sets-sat4j: de.featjar.formula.analysis.cli.AtomicSetsCommand
            solutions-sat4j: de.featjar.formula.analysis.cli.SolutionsCommand
            solution-count-sat4j: de.featjar.formula.analysis.cli.SolutionCountCommand
            t-wise-sat4j: de.featjar.formula.analysis.cli.TWiseCommand
            projection-sat4j: de.featjar.formula.transform.cli.ProjectionCommand
            print-sat4j: de.featjar.formula.transform.cli.PrintCommand
            convert-cnf-format-sat4j: de.featjar.formula.transform.cli.ConvertCNFFormatCommand
            convert-format-sat4j: de.featjar.formula.transform.cli.ConvertFormatCommand

If you want more details of one specific command, you can again use the `--help` flag.
Here is an example:

    $ java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar print-sat4j --help
    Usage: java -jar feat.jar [--command <command> | <shortcut>] [--<flag> | --<option> <value>]...

    Help for de.featjar.formula.transform.cli.PrintCommand
            Prints the formula in a readable format using SAT4J.
    
            General options:
                    --config <value1,value2,...>: The names of configuration files (default: [])
                    --config_dir <value>: The path to the configuration files
                    --command <value>: Command to execute
                    --help: Print usage information
                    --version: Print version information
                    --info-file <value>: Path to info log file
                    --error-file <value>: Path to error log file
                    --log-info <value1,value2,...>: Message types printed to the info stream (message, error, warning, info, debug, progress) (default: [MESSAGE, INFO, PROGRESS])
                    --log-error <value1,value2,...>: Message types printed to the error stream (message, error, warning, info, debug, progress) (default: [ERROR])
                    --log-info-file <value1,value2,...>: Message types printed to the info file (message, error, warning, info, debug, progress) (default: [MESSAGE, INFO, DEBUG])
                    --log-error-file <value1,value2,...>: Message types printed to the error file (message, error, warning, info, debug, progress) (default: [ERROR, WARNING])
            
            Options of command de.featjar.formula.transform.cli.PrintCommand:
                    --enforce-parentheses <value>: Enforces parentheses.
                    --enquote-whitespace <value>: Enquotes whitespace.
                    --format <value>: Defines the symbols.
                    --input <value>: Path to input file(s)
                    --newline <value>: Defines the new line string.
                    --notation <value>: Defines the notation. Possible options: [INFIX, PREFIX, POSTFIX, TREE]
                    --separator <value>: Defines the separator string.
                    --tab <value>: Defines the tab string.

### Command shortcuts

Typing commands can be somewhat verbose. Therefore, it is possible to discard the `--command` option and use shortcuts instead.
You can get a list of all shortcuts by typing using the `--help` flag as shown above.
For example, when executing the `de.featjar.formula.transform.cli.PrintCommand`, you could instead type:

    java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar print-sat4j ...

### Full example of the `print-sat4j` command

Every option and flag is specified in this example. You can get an overview of all options by using the `--help` flag as shown above.

     java -jar formula-analysis-sat4j/build/libs/formula-analysis-sat4j-0.1.1-SNAPSHOT-all.jar print-sat4j --input formula/src/testFixtures/resources/GPL/model.xml --tab tab --notation PREFIX --separator separator --format de.featjar.formula.io.textual.JavaSymbols --newline newline --enforce-parentheses --enquote-whitespace

The output will be printed in the console because no `--output` option was set:

    &&(||(GPL) ||(Result{!, []}MainGpl GPL) ||(Result{!, []}HiddenGtp MainGpl) ||(Result{!, []}MainGpl HiddenGtp) ||(Result{!, []}DirectedWithEdges HiddenGtp) ||(Result{!, []}DirectedWithNeighbors HiddenGtp) ||(Result{!, []}DirectedOnlyVertices HiddenGtp) ||(Result{!, []}UndirectedWithEdges HiddenGtp) ||(Result{!, []}UndirectedWithNeighbors HiddenGtp) ||(Result{!, []}UndirectedOnlyVertices HiddenGtp) ||(Result{!, []}HiddenGtp DirectedWithEdges DirectedWithNeighbors DirectedOnlyVertices UndirectedWithEdges UndirectedWithNeighbors UndirectedOnlyVertices) ||(Result{!, []}DirectedWithEdges Result{!, []}DirectedWithNeighbors) ||(Result{!, []}DirectedWithEdges Result{!, []}DirectedOnlyVertices) ||(Result{!, []}DirectedWithEdges Result{!, []}UndirectedWithEdges) ||(Result{!, []}DirectedWithEdges Result{!, []}UndirectedWithNeighbors) ||(Result{!, []}DirectedWithEdges Result{!, []}UndirectedOnlyVertices) ||(Result{!, []}DirectedWithNeighbors Result{!, []}DirectedOnlyVertices) ||(Result{!, []}DirectedWithNeighbors Result{!, []}UndirectedWithEdges) ||(Result{!, []}DirectedWithNeighbors Result{!, []}UndirectedWithNeighbors) ||(Result{!, []}DirectedWithNeighbors Result{!, []}UndirectedOnlyVertices) ||(Result{!, []}DirectedOnlyVertices Result{!, []}UndirectedWithEdges) ||(Result{!, []}DirectedOnlyVertices Result{!, []}UndirectedWithNeighbors) ||(Result{!, []}DirectedOnlyVertices Result{!, []}UndirectedOnlyVertices) ||(Result{!, []}UndirectedWithEdges Result{!, []}UndirectedWithNeighbors) ||(Result{!, []}UndirectedWithEdges Result{!, []}UndirectedOnlyVertices) ||(Result{!, []}UndirectedWithNeighbors Result{!, []}UndirectedOnlyVertices) ||(Result{!, []}TestProg MainGpl) ||(Result{!, []}MainGpl TestProg) ||(Result{!, []}Alg MainGpl) ||(Result{!, []}MainGpl Alg) ||(Result{!, []}Number Alg) ||(Result{!, []}Connected Alg) ||(Result{!, []}StrongC Alg) ||(Result{!, []}StronglyConnected StrongC) ||(Result{!, []}StrongC StronglyConnected) ||(Result{!, []}Transpose StrongC) ||(Result{!, []}StrongC Transpose) ||(Result{!, []}Cycle Alg) ||(Result{!, []}MSTPrim Alg) ||(Result{!, []}MSTKruskal Alg) ||(Result{!, []}Alg Number Connected StrongC Cycle MSTPrim MSTKruskal) ||(Result{!, []}Src MainGpl) ||(Result{!, []}MainGpl Src) ||(Result{!, []}BFS Src) ||(Result{!, []}DFS Src) ||(Result{!, []}Src BFS DFS) ||(Result{!, []}BFS Result{!, []}DFS) ||(Result{!, []}HiddenWgt MainGpl) ||(Result{!, []}MainGpl HiddenWgt) ||(Result{!, []}WeightOptions HiddenWgt) ||(Result{!, []}HiddenWgt WeightOptions) ||(Result{!, []}WeightedWithEdges WeightOptions) ||(Result{!, []}WeightedWithNeighbors WeightOptions) ||(Result{!, []}WeightedOnlyVertices WeightOptions) ||(Result{!, []}Wgt MainGpl) ||(Result{!, []}MainGpl Wgt) ||(Result{!, []}Weighted Wgt) ||(Result{!, []}Unweighted Wgt) ||(Result{!, []}Wgt Weighted Unweighted) ||(Result{!, []}Weighted Result{!, []}Unweighted) ||(Result{!, []}Gtp MainGpl) ||(Result{!, []}MainGpl Gtp) ||(Result{!, []}Directed Gtp) ||(Result{!, []}Undirected Gtp) ||(Result{!, []}Gtp Directed Undirected) ||(Result{!, []}Directed Result{!, []}Undirected) ||(Result{!, []}Implementation MainGpl) ||(Result{!, []}MainGpl Implementation) ||(Result{!, []}OnlyVertices Implementation) ||(Result{!, []}WithNeighbors Implementation) ||(Result{!, []}WithEdges Implementation) ||(Result{!, []}Implementation OnlyVertices WithNeighbors WithEdges) ||(Result{!, []}OnlyVertices Result{!, []}WithNeighbors) ||(Result{!, []}OnlyVertices Result{!, []}WithEdges) ||(Result{!, []}WithNeighbors Result{!, []}WithEdges) ||(Result{!, []}Base MainGpl) ||(Result{!, []}MainGpl Base) ||(Result{!, []}GPL MainGpl) ||(Result{!, []}Number Src) ||(Result{!, []}Number Gtp) ||(Result{!, []}Connected Src) ||(Result{!, []}Connected Undirected) ||(Result{!, []}StrongC DFS) ||(Result{!, []}StrongC Directed) ||(Result{!, []}Cycle DFS) ||(Result{!, []}Cycle Gtp) ||(Weighted Result{!, []}MSTPrim) ||(Weighted Result{!, []}MSTKruskal) ||(Undirected Result{!, []}MSTPrim) ||(Undirected Result{!, []}MSTKruskal) ||(Result{!, []}MSTPrim Result{!, []}MSTKruskal) ||(Result{!, []}MSTKruskal WithEdges) ||(Result{!, []}OnlyVertices Result{!, []}Weighted WeightedOnlyVertices) ||(Result{!, []}WeightedOnlyVertices Weighted) ||(Result{!, []}WeightedOnlyVertices OnlyVertices) ||(Result{!, []}WithNeighbors Result{!, []}Weighted WeightedWithNeighbors) ||(Result{!, []}WeightedWithNeighbors Weighted) ||(Result{!, []}WeightedWithNeighbors WithNeighbors) ||(Result{!, []}WithEdges Result{!, []}Weighted WeightedWithEdges) ||(Result{!, []}WeightedWithEdges Weighted) ||(Result{!, []}WeightedWithEdges WithEdges) ||(Result{!, []}OnlyVertices Result{!, []}Directed DirectedOnlyVertices) ||(Result{!, []}DirectedOnlyVertices Directed) ||(Result{!, []}DirectedOnlyVertices OnlyVertices) ||(Result{!, []}WithNeighbors Result{!, []}Directed DirectedWithNeighbors) ||(Result{!, []}DirectedWithNeighbors Directed) ||(Result{!, []}DirectedWithNeighbors WithNeighbors) ||(Result{!, []}WithEdges Result{!, []}Directed DirectedWithEdges) ||(Result{!, []}DirectedWithEdges Directed) ||(Result{!, []}DirectedWithEdges WithEdges) ||(Result{!, []}OnlyVertices Result{!, []}Undirected UndirectedOnlyVertices) ||(Result{!, []}UndirectedOnlyVertices Undirected) ||(Result{!, []}UndirectedOnlyVertices OnlyVertices) ||(Result{!, []}WithNeighbors Result{!, []}Undirected UndirectedWithNeighbors) ||(Result{!, []}UndirectedWithNeighbors Undirected) ||(Result{!, []}UndirectedWithNeighbors WithNeighbors) ||(Result{!, []}WithEdges Result{!, []}Undirected UndirectedWithEdges) ||(Result{!, []}UndirectedWithEdges Undirected) ||(Result{!, []}UndirectedWithEdges WithEdges))


## How to write commands

### Adding a new command

You can add a new command by creating a class that implements the `ICommand` interface.
The `run` method will be called when the command is executed.
The `getShortName` method returns the command shortcut.

    public class MyCommand implements ICommand {
        public void run(OptionList optionParser) { ... }
        public String getShortName() { ... }
    }

You can create a flag or an option for your command like this:
    
    // Example for string option
    public static final Option<String> MY_OPTION = new Option<>("option_name", Option.StringParser)
        .setDescription("My option")
        .setRequired(true)
        .setValidator(str -> str.contains("hi"));

    // example for flag
    public static final Option<Boolean> MY_FLAG = new Flag("flag_name")
        .setDescription("My flag")

    // list option
    public static final ListOption<String> MY_LIST_OPTION = new ListOption<>("literals", Option.StringParser);

You can also create a list option where you separate the elements with a comma.
After creating the command, open the file `<package>/src/main/resources/extensions.xml`.
There you must add the classpath of the command:

    <?xml version="1.0"?>
    <extensions>
        <point id="de.featjar.base.cli.Commands">
            ...
            <extension id="de.featjar.formula. ... .MyCommand" />
            ...
        </point>
    </extensions>

Finally, you must run the gradle build or assemble command to compile a new JAR file.
An example for this is shown in the next subsection.

### Modifying a command

Before testing any changes that you made, you must first rebuild the modules that are affected.
For example, if you changed a class in the *formula-analysis-sat4j* module, you must first run:

    # assemble module and run all tests
    ./gradlew :formula-analysis-sat4j:build
    
    # assemble module and skip all tests
    ./gradlew :formula-analysis-sat4j:assemble

## Contributors

FeatJAR development team:

* [Thomas Thüm](https://www.uni-ulm.de/in/sp/team/thuem/) (University of Ulm, Germany)
* [Sebastian Krieter](https://www.uni-ulm.de/in/sp/team/sebastian-krieter/) (University of Ulm, Germany)
* [Elias Kuiter](https://www.dbse.ovgu.de/Mitarbeiter/Elias+Kuiter.html) (University of Magdeburg, Germany)
* Andreas Gerasimow (University of Ulm, Germany)

Further contributors and former project members:

* Katjana Herbst (University of Ulm, Germany)
* Daniel Hohmann (University of Magdeburg, Germany)
* Timo Zuccarello (University of Ulm, Germany)
