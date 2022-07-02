# FPL - Functional Programming Language

FPL is an experimental functional programming language. Don't use it in production (yet), currently it's just an experiment.
Some of its features:
* Lisp like syntax
* Immutable lists (with a new - hopefully efficient - implementation)
* Mutable maps (I was too lazy to write a good immutable version so far)
* Lazy evaluation. This can be used to implement some Lisp features without using a macro system.
* Currying/Schönfinkeln
* Variable parameter lists for builtin and other functions
* Adapter functions to call Java
* Comments with a feature similar to Javadoc
* 100% test coverage 
* Builtin HTTP server, so you can use Postman as you REPL tool

## Syntax

All sourcecode has to be UTF-8 encoded. 

### Numbers

Numbers can be integral numbers (64 bit) or floating point numbers (IEEE double). 
The syntax is the same as in Java (without the _ feature to structure numbers.

### Symbol and Strings

Symbols are sequences of non whitespace characters except ` `, `"`, `'`, `(`, `)`, `[`, `]`, `{`, `}`, `:`, `;`.
A symbol must not start with `'`, as this is a shortcut for the function `qoute`.

Strings are delimited by `"`. Quoting works as in Java with `\`.

### Keywords

There is only one keyword: `nil`. It stands for "nothing", like null in Java.

### Lists

A List starts with `(`, followed by the elements of the list, followed by `)`. A List may be empty: `()`.

### Quote

The character `'` in front of a list blocks the evaluation of the list. This is a short cut for the function `qoute`.

### Comments

Comments start with `;` and end with the next newline. One or several comment lines before a symbol are collected and associated
with that symbol. This can be used like Javadoc.

A special case is `#`: It is only start of a comment, when it is the first character of the source code, allowing
to start fpl as an interpreter on Unix like systems: Write #!/usr/bin/fpl as first line and install fpl in /usr/bin/fpl.
Then you can start fpl scripts like shell scripts.

## Command Line Interpreter

When you have build the jar file, you can interpreted one or more files by calling it from the command line, example:
```
java -jar fpl.jar file.fpl another.fpl
```
Results are written to standard output.

When you want to switch off the output of all intermediate results, set the variable `silent` in the global scope to 1 (true).
To switch them on, set `silent` to 0 (false).

## Documentation of Standard Functions

* [Arithmetic and comparison](doc/arithmetic-comparison.md)
* [Variables, assignments, and scopes](doc/variables-assignments-scopes.md)
* [Lambdas and Functions](doc/lambdas-and-funcions.md)
* [Control Structures (if, try-catch)](doc/control-structures.md)
* [Loops (while, map, reduce)](doc/map-and-loops.md)
* [List functions](doc/lists.md)
* [String functions](doc/strings.md)
* [Dictionaries](doc/dictionaries.md)
* [Classes and Objects](doc/classes-and-objects.md)
* [Java bridge](doc/java.md)
* [Functions for parallelism](doc/parallel.md)
* [Input/Output functions](doc/io.md)
* [HTTP server as REPL](doc/http-server-repl.md)

## Further Reading

If you want to see larger examples, have a look at my [solutions](https://github.com/rbutenuth/advent-of-code) of the [Advent of Code 2021](https://adventofcode.com/2021)
.
