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

## Command Line Interpreter

When you have build the jar file, you can interpreted one or more files by calling it from the command line, example:
```
java -jar fpl.jar file.fpl another.fpl
```
Results are written to standard output.

## Further Reading...

* [Arithmetic and comparison](doc/arithmetic-comparison.md)
* [Variables, assignments, and scopes](doc/variables-assignments-scopes.md)
* [Lambdas and Functions](doc/lambdas-and-funcions.md)
* [Control Structures (if, loops, try-catch)](doc/control-structures.md)
* [List functions](doc/lists.md)
* [String functions](doc/strings.md)
* [Dictionaries](doc/dictionaries.md)
* [Classes and Objects](doc/classes-and-objects.md)
* [Java bridge](doc/java.md)
* [Functions for parallelism](doc/parallel.md)
* [Input/Output functions](doc/io.md)
* [HTTP server as REPL](doc/http-server-repl.md)
