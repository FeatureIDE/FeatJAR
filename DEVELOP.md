# Information for Developers

When developing FeatJAR code, we recommend to respect the following coding conventions to ensure consistency and high-quality code.

### [Naming Things](https://martinfowler.com/bliki/TwoHardThings.html)

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

### Documentation

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

### Tests

`TODO`

### Miscellaneous

* Avoid global mutable state (i.e., non-final `static` fields).
  When it cannot be avoided, implement an `IExtension` and register it (e.g., in `Initializers`).
  This way, global mutable state is at least encapsulated in a singleton and its lifetime is limited by the (un-)installer in `Extensions`.
* Avoid `private` fields and methods.
  Use `protected` instead to allow API extensions. 
* [How To Design A Good API and Why it Matters](https://www.youtube.com/watch?v=aAb7hSCtvGw)
* `hashCode`, `equals`, `clone`, `serialVersionUID`, `toString` format: `TODO`
* Avoid returning `null` and throwing exceptions.
  Instead, return an `Optional` for planned absence of values or `Result` for planned or erroneous absence of values.
  Exceptions to this rule:
  * You can return `null` when implementing an optional feature in an extension (i.e., a default method in an interface that inherits `Extension`).
    If you do so, still document it (`{@return ..., if any}`).
  * You can throw (preferably checked) exceptions in `void` methods to avoid introducing a necessarily empty return value.
    If you do so, document it, especially for unchecked exceptions (`@throws ... when ...`).
* Concrete implementations of `IComputation`, `IAnalysis`, and `ITransformation` should be named `Compute*`, `Analyze*`, and `Transform*`, respectively.
* Variables of type `IComputation` should be named without `computation` suffix.
  Use the helpers in `Computations` to convert between (a)synchronous computation modes.
* Use `LinkedHashMap` and `LinkedHashSet` instead of `HashMap` and `HashSet` to preserve order and guarantee determinism.