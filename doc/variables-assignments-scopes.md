# Definitions, Assignments, and Scopes

## def, set, and put

Even when you design an untyped language, you can protect against some errors.
For this reasons fpl distinguishes between `def`, `set`, and `put`.

For example, `(def x 42)` associates the symbol `x` with the value `42` in the current scope. 
This works only when `x` is not associated with any value before, otherwise you get the error message
`Duplicate key: x`. This avoids to overwrite a variable by accident.

Whenever you want to change the value of an existing variable, you should use `set`. This works on existing variables only. 
When you try `(set foo "bar")` without defining `foo` before, the error message will be 
`No value with key foo found`.   

## Short Cut for Functions: def-function

A function in fpl is a lambda expression. To define a function to square a number, you just have to write
`(lambda (x) (* x x))`. To apply this to the number `7`, just write:
`((lambda (x) (* x x)) 7)` and you get the result 49. 

In most cases you don't want to write down a function definition every time you want to apply it to a value.
To avoid this, you can assign the lambda expression to a symbol:
`(def square (lambda (x) (* x x)))`
Or with the short cut `def-function`:
`(def-function square (x) (* x x))`. Looks like Lisp, but is fpl.
To apply this to a number, write: `(square 4)`

To make things a bit more formal: The function `lambda` defines a lambda expression. The first parameter
of `lambda` is a list of symbols, the parameters of the function to be defined. The following parameters 
are expressions to be evaluated one after the other. The result of the last expression is the result of the 
function. The other expressions are only useful when they create a side effect (I know, this is not really
pure functional programming.


## Nested Scopes

-----

TODO: put, def-global, def-function etc.

(put ++ nil)
(put foo nil)
(put square nil)

(def-function square (x) * x x)

(def-function ++ (x) (set (quote x) (+ x 1)))

(def foo 3)

foo

(++ foo)

foo

(list nil nil nil)


(put square nil)
(def-function square (x) * x x)
