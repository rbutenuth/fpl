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

Symbols are sequences of non whitespace characters except `"`, `(`, `)`, `:`. A symbol must not start with `'`.

Strings are delimited by `"`. Quoting works as in Java with \.

### Keywords

There is only one keyword: `nil`. It stands for "nothing", like null in Java.

### Lists

A List starts with `(`, followed by the elements of the list, followed by `)`. A List may be empty: `()`.

### Objects / Dictionaries

Objects are delimited by `{` and `}`. Inside that you can define zero to n mappings, consisting of a  symbol (key), `:` and a value.
Don't confuse it with JSON syntax: The keys are not surrounded by `"` and there is no `,` between the key pairs.

### Quote

The character `'` in front of a list blocks the evaluation of the list. This is a short cut for the function `qoute`.

### Comments

Comments start with `;` and end with the next newline. One or several comment lines before a symbol are collected and associated
with that symbol. This can be used like Javadoc.

## Further Reading...

* [Arithmetic and comparison](doc/arithmetic-comparison.md)
* [Built-in HTTP server](doc/http-server.md)
* [Variables, assignments, and scopes](doc/variables-assignments-scopes.md)
* [Lambdas and Functions](doc/lambdas-and-funcions.md)
* [List functions](doc/lists.md)
* [Dictionaries](doc/dictionaries.md)
* [Classes and Objects](doc/classes-and-objects.md)
* [Java wrapper](doc/java.md)
* [Function for parallelism](doc/parallel.md)