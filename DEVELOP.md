# Information for Developers

When developing FeatJAR code, we recommend to respect the following coding conventions to ensure consistency and high-quality code.

## [Naming Things](https://martinfowler.com/bliki/TwoHardThings.html)

* Rule of thumb: Prefer good naming (without documentation) over bad naming (with documentation).
* Most classes are
  * things, which actors operate on (e.g., `Tree`),
  * actors, which operate on things (e.g., `TreeVisitor`), or
  * buckets, which loosely collect related methods (e.g., `Trees`).

  This should be reflected in their naming. 
* Prepend names of abstract classes with `A` and interfaces with `I`, so there is room for a canonical implementation (e.g., `ITree` and its primary implementation `Tree`).
  Avoid using inner classes and interfaces - if using them, do not prepend their names.
* Abbreviations should be capitalized (e.g., prefer `ComputeCNFFormula` over `ComputeCnfFormula`).
  Only use well-known abbreviations.
* We recommend to move complex algorithms for manipulating a data structure `<Struct>` into a class with static methods `<Struct>s` (e.g., `Trees` manipulates `ITree` instances).
  For common algorithms, consider adding a convenience method to `<Struct>` to ensure obvious visibility for API consumers (e.g., `ITree#traverse(TreeVisitor)` as a shorthand for `Trees#traverse(ITree, TreeVisitor)`).
  The rationale is to keep the data structure class free from algorithm details (e.g., traversal iterators) and make better use of generic types.
* The same naming convention is also applied for extensions (e.g., `Thing`) and their extension points (e.g., `Things`).

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


## Documentation

* All Java classes in FeatJAR should be documented with a JavaDoc comment above the class, including the purpose of the class and its author(s).
* Depending on the subjective importance of the class, either
  * all attributes and methods should be documented, or
  * all public and protected attributes and methods should be documented (recommended), or
  * no attributes and methods should be documented.
  
  That is, avoid documenting an arbitrary selection of methods.
  The rationale is to keep the documentation clear, concise, and correct. 
* Use the JavaDoc tags `@author` (for classes), `@param`, `@return`, `@throws`, and `@inheritDoc` (when extending documentation).
  For further guidelines, see [here](https://blog.joda.org/2012/11/javadoc-coding-standards.html).
* When documenting a method, consider its most important usage example and typical failure modes.
* As an example for appropriate documentation, refer to the `base` module.
* Tests need not be documented using JavaDoc.
  Instead, write small tests with telling names.

## Tests

`TODO`

## Miscellaneous

* Avoid global mutable state (i.e., non-final `static` fields).
  When it cannot be avoided, implement an `IExtension` and register it (e.g., in `Initializers`).
  This way, global mutable state is at least encapsulated in a singleton and its lifetime is limited by the (un-)installer in `Extensions`.
* Avoid `private` fields and methods.
  Use `protected` instead to allow API extensions. 
* [How To Design A Good API and Why it Matters](https://www.youtube.com/watch?v=aAb7hSCtvGw)
* Do not implement `Object.clone`, which cannot be used reliably for all classes.
  Instead, write copy constructors.
* Implement `Object.equals` and `Object.hashCode` where necessary (e.g., for objects cached in computations), but always in pairs. 
* `serialVersionUID`, `toString` format: `TODO`
* Avoid returning `null` and throwing exceptions.
  Instead, return a `Result` for planned or erroneous absence of values.
  Exceptions to this rule:
  * You can return `null` when implementing an optional feature in an extension (i.e., a default method in an interface that inherits `Extension`).
    If you do so, still document it (`{@return ..., if any}`).
  * You can throw (preferably checked) exceptions in `void` methods to avoid introducing a necessarily empty return value.
    If you do so, document it, especially for unchecked exceptions (`@throws ... when ...`).
* Concrete implementations of `IComputation`, `IAnalysis`, and `ITransformation` should be named `Compute*`, `Analyze*`, and `Transform*`, respectively.
* Variables of type `IComputation` should be named without `computation` suffix.
  Use the helpers in `Computations` to convert between (a)synchronous computation modes.
* Use `LinkedHashMap` and `LinkedHashSet` instead of `HashMap` and `HashSet` to preserve order and guarantee determinism.
* 