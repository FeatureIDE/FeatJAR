# Information for creating extensions

## Extension Points

FeatJAR supports the following extension points:
- Commands
- Binaries
- Formats
- Initializers

You must register all new extension points in the `<package>/src/main/resources/extensions.xml` file of your package.

## Modifying extensions

Before testing any changes that you made, you must first rebuild the modules that are affected.
For example, if you changed a class in the *formula-analysis-sat4j* module, you must first run:

    # assemble module and run all tests
    ./gradlew :formula-analysis-sat4j:build
    
    # assemble module and skip all tests
    ./gradlew :formula-analysis-sat4j:assemble

## How to write commands

You can add a new command by creating a class that implements the `ICommand` interface or extends the `ACommand` class.
The `run` method will be called when the command is executed.
The `getShortName` method returns the command shortcut.

    public class MyCommand implements ICommand {
        public void run(OptionList optionParser) { ... }
        public String getShortName() { ... }
    }

You can create a flag or option for your command like this:

    // Example for string option
    public static final Option<String> MY_OPTION = Option.newOption("option_name", Option.StringParser)
        .setDescription("My option")
        .setRequired(true)
        .setValidator(str -> str.contains("hi"));

    // example for flag
    public static final Option<Boolean> MY_FLAG = Option.newFlag("flag_name")
        .setDescription("My flag")

You can also create a list option where you separate the elements with a comma.

    // list option
    public static final ListOption<String> MY_LIST_OPTION = Option.newListOption("literals", Option.StringParser);

After creating the command, you must also add an extension point.
Open the file `<package>/src/main/resources/extensions.xml`.
There you can add the classpath of the command:

    <?xml version="1.0"?>
    <extensions>
        ...
        <point id="de.featjar.base.cli.Commands">
            ...
            <extension id="de.featjar.formula. ... .MyCommand" />
            ...
        </point>
        ...
    </extensions>

Finally, you must run the gradle build or assemble command to compile a new JAR file.

## How to write formats

You can add a new format by creating a class that implements the `IFormat` interface.
The `parse` method will be called when you want to parse an input (string) to a class.
The `serialize` method will be called if you want to serialize a class.
Both methods are optional.


    public class MyClassFormat implements IFormat<MyClass> {
 
        @Override
        public Result<MyClass> parse(AInputMapper inputMapper) {
            String content = inputMapper.get().text();
            ...
        }
    
        @Override
        public Result<String> serialize(MyClass myClass) {
            ...
        }
    
        @Override
        public boolean supportsParse() {
            return true;
        }
    
        @Override
        public boolean supportsSerialize() {
            return true;
        }
    
        @Override
        public String getFileExtension() {
            return "xml";
        }
    
        @Override
        public String getName() {
            return "Extensible Markup Language";
        }
    }

After creating the format, you must also add an extension point.
Open the file `<package>/src/main/resources/extensions.xml`.
There you can add the classpath of the format:

    <?xml version="1.0"?>
    <extensions>
        ...
        <point id="de.featjar.feature.model.io.FeatureModelFormats">
            <extension id="de.featjar.feature.model. ... .MyClassFormat" />
        </point>
        ...
    </extensions>

Finally, you must run the gradle build or assemble command to compile a new JAR file.