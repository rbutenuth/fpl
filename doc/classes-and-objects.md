# Classes and Objects 

## Introduction

The core of FPL is functional, but some simple extensions allow object oriented programming with classes and instances (objects).
How does it work? What is a class or an object? Both are not more than slightly extended [dictionaries](dictionaries.md).
The difference between a dictionary and a class/object is the nested scoping: A dictionary is just a map or scope, but it
has no parent scope. Whenever a key is looked up in a dictionary, it is either found in this dictionary or nil is returned.

On the other hand: A class or an object always has a parent scope. This can be another class or object or the global scope
of FPL. That's the first part of the trick: An object is bound to its class by the scoping rules: The parent scope of the
object is the class. You can create a new object with the function new-instance. Whenever you execute this function within
a class, you create an object which "belongs" to this class. The coupling is just done by the scoping rules.

With the help of the function sub-class it is possible to create a new class and to set the parent class in an explicit
way. There is no difference between classes and objects on a technical level: It's all just scoping.

How can you use classes and how can you call methods (functions defined in a class)? When designing FPL, I wanted a syntax 
similar to existing object oriented languages without complicated syntax. The solution is quite simple: Objects and classes 
are functions! When called, they evaluate (in the scope of the class or object) their first argument. 
It must evaluate to a function. Then this function is called within the scope of the class or object. So the syntax is:

```
(some-object-or-class evaluates-to-function first-real-arg second-real-arg)
```
Not a big difference to Java:

```
someObjectOrClass.evaluatesToFunction(firstRealArg, secondRealArg);
```

The order of all elements is the same, just the special characters like dots and parentheses are different. 

For the object and class related functions the same conventions like in other contexts of FPL regarding "set",
"def" etc. apply. Some of the functions have no special versions for classes and objects, but you can use
the functions related to dictionaries as a replacement. 

The functions explained in the following chapter are only special in the way they can handle the scopes.

## Functions

### class
Create a new scope and execute the given code within it. Result is the class.

Syntax: 
```
(class code...)
```

### def-class
Create a new scope and execute the given code within it. Assign the resulting class to "name"

Syntax: 

```
(def-class name code...)
```

Example:

```
(def-class my-class 

	(def-field foo "bar")
	
)
```

Create a new class, the class has one field (a mapping in its scope) called "foo". The field has
the value "bar". So far the class has no methods. So it's more like an object.


### def-field
Assign value in the next object scope, it must be unassigned before. nil as value not allowed

Example: 

```
(def-field symbol value)
```

This functions must be called within the scope of an object or class (or in a sub scope of that). 
It will assign the value to the symbol.  

### def-sub-class

Define a class and set the parent of the class. This makes it possible to nest classes regarding
their scopes without nesting them in the source code. You can later create a sub class from a 
class without having access to the code of the extended class.

Example: 

```
(def-sub-class name parent code...)
```

### new-instance
Create an instance of an object.

Example: 

```
(new-instance key-value-pair...)
```

This must be called in the scope of a class or object. It creates a new object which is nested within
the scope where new-instance is executed. The number of parameters must be even (a set of pairs). The
first part of a pair must be a symbol or evaluate to a string, it determines the name of a field.
The second part must be evaluate to a value (not nil), it determines the value of a field. 

Example:

```
(def-class my-class 

	(def-function new (a b)
		(new-instance "a" a "b" b)
	)
	
	(def-field key 42)
)

(def my-object (my-class new 6 7))
```

This example defines a class "my-class" with a field "key" (with value 42) and a method "new".

After that, "new" is called with 6 and 7 as parameters, this creates a new object of the class
"my-class" with fields "a" (value 6) and "b" (value 7). So you can create constructors in 
classes with static methods that act like factories.


### sub-class
Create a new scope and execute the given code within it, set parent to parameter.

Example: 

```
(sub-class parent code...)
```

### this
The next object in the scope chain, can be nil when not within an object context.

Example: 

```
(this)
```
