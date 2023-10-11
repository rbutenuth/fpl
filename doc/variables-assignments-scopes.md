# Definitions, Assignments, and Scopes

## def, set, and put

Even when you design an untyped language, you can protect against some errors.
For this reasons FPL distinguishes between `def`, `set`, and `put`.

For example, `(def x 42)` associates the symbol `x` with the value `42` in the current scope. 
This works only when `x` is not associated with any value before, otherwise you get the error message
`Duplicate key: x`. This avoids to overwrite a variable by accident.

Whenever you want to change the value of an existing variable, you should use `set`. This works on existing variables only. 
When you try `(set foo "bar")` without defining `foo` before, the error message will be 
`No value with key foo found`.

How do symbols look like? There are very few restrictions: They can't start with a digit and they can't contain any
of the other special characters used by the FPL syntax: ` `, `"`, `(`, `)`, `[`, `]`, `{`, `}`, `:`.
Some of the special characters (`[]{}:`) are reserved for future use. A symbol must not start with `'`, as this is a shortcut for the function `quote`. 
So `++` is a valid symbol.

## put with Pattern Matching

The function `match-put` allows pattern matching in combination with deconstruction of lists. It's useful when you call functions
which return a list of results. It returns if the match is successful, so you can combine it with `cond`.

```
(match-put list-with-symbols list-with-values)
```

Examples:

```
(match-put (x y) '("foo" "bar"))
```

Assigns "foo" to `x` and "bar" to `y`, returns 1 (true)

```
(match-put (x y) '("foo" "bar" "baz"))
```

Assigns nothing, returns 0 (false)

Note that the first parameter of match-put is _not_ evaluated. It must be a symbol or a (nested) list of symbols.


## Short Cut for Functions: def-function

A function in FPL is a lambda expression. To define a function to square a number, you just have to write
`(lambda (x) (* x x))`. To apply this to the number `7`, just write:
`((lambda (x) (* x x)) 7)` and you get the result 49. 

In most cases you don't want to write down a function definition every time you want to apply it to a value.
To avoid this, you can assign the lambda expression to a symbol:
`(def square (lambda (x) (* x x)))`
Or with the short cut `def-function`:
`(def-function square (x) (* x x))`.
To apply this to a number, write: `(square 4)`

To make things a bit more formal: The function `lambda` defines a lambda expression. The first parameter
of `lambda` is a list of symbols, the parameters of the function to be defined. The following parameters 
are expressions to be evaluated one after the other. The result of the last expression is the result of the 
function. The other expressions are only useful when they create a side effect (I know, this is not really
pure functional programming.


## Nested Scopes

When you are evaluating expressions "top level", you are operating on the global scope. When you are executing
code in a function (defined by you), it executes within it's own scope. As in most programming languages, `a`
in the global scope can be hidden by a parameter or variable `a` within a functions. So when you define
a variable with `def` within a function, it's visible in this function only. 

When you want to define a global variable within a function, you have to use `def-global` or `put-global`
instead. They are similar to their counterparts without the `-global` suffix, but they always operate on 
the global scope, not on the current local scope.

Is there a `set-global`? No. Why? Because it's not needed! When you use set, it starts it search in the
current scope and walks up the chain up to the global scope. In the first scope where the symbol is bound,
it will be bound (set) to the new value. When it is not found, an error will be thrown.

So let's have a look on an example:

```
(def-function my-function (x) 
	(set a x)
	(def b x)
	(def-global c x)
)

(def a 42)
(my-function 43)
```

What is happening here? The function has one parameter `x` and uses `set`, `def` and `def-global` with
the parameter and binds the value to the symbols `a`, `b`, and `c`. Before we call the function, we set
the value of `a` to 42 in the global scope. So when we call the function with 43 as argument, `a` in the
global scope is set to 43. `b` is set to 43, too, but that is only visible within the function. The last
action is to set `c` in the global scope to 43. When we execute 

```
(list "a" a "b" b "c" c)
```

we will get `("a" 43 "b" nil "c" 43)` as a result.

## Additional Notes

The first parameter of all the "assignment functions" is usually a symbol. When it is not a symbol, it will
be evaluated. In case the result is a string, it is converted to a symbol. This way you can generate the 
name of the assignment target dynamically with a function (which has to return a string). 

What if you try to assign a value to a parameter of a function? This is not allowed and will cause an 
error. So the bad style of assigning values to parameters is not legal in FPL.


