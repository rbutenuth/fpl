# Lambdas and Functions

You call a functions by evaluating a list. The first element of the list is evaluated. When it is a function,
it will be called with the remaining list elements as parameters. So

```
(+ 3 4)
```

Evaluates to `7` in the following steps: First, the symbol `+` is evaluated. The result is the built-in 
function "add". It will evaluate its parameters `3` and `4`. When you evaluate a number in fpl, it will evaluate
to itself. After that, the two numbers will be added and the result `7` is returned.

One of the steps sounds to be irrelevant here but will be important later: The parameters are passed to 
the function "as is". They are _not_ evaluated at this moment! They are evaluated when they are needed within
the function. For this reason, all parameters are passed together with their context to a function and evaluated
on demand. That's called lazy evaluation and important for some features which will be discussed later.

## Define Lambdas and Functions

Everything is fine so far when you just want to call built-in functions. But that's not the purpose of a
language: You want to extend the existing features and capabilities with your own functions. 

Functions in fpl are defined as lambda expressions. Of course, lambda itself is a function. To create a lambda
expression to square its parameter, write:

```
(lambda (x) (* x x))
```

`lambda` is a function with two or more parameters: The first parameter is the list of parameters for the
defined function. It can be empty, in this case the function can only operate on global values. (Which
makes it a non pure function.) The other parameters are expressions which will be evaluated in their 
order of appearance. In this example it is only one expression `(* x x)` which will multiply the 
parameter `x` with itself. The result of the last expression evaluation will be returned by the lambda.

How can you make use of this lambda expression? Call it as a function:

```
((lambda (x) (* x x)) 7)
```

The lambda expression is wrapped into a list, followed by one parameter, the number `7`. What happens
with this list? The first element evaluates to a function - the lambda expression - which will be 
applied to the one and only parameter. This will evaluate to `49`.

So far we have written an anonymous function. Can we give it a name? Of course: Just bind it to a symbol
with help of the `def` function:

```
(def square (lambda (x) (* x x)))
```

Now the symbol `square` will evaluate to the lambda expression, so we can write `(square 7)` to
get the result `49`.

As a shortcut for `def` and `lambda` you can use the following form: 

```
(def-function square (x) (* x x))
```

It combines the function definition with a lambda expression and its binding to a symbol. It fails
(like `def`), when the symbol is already bound in the current scope. 


## Lazy Evaluation

TODO

## Optional Arguments and Variable Argument Lists

TODO

## Currying

TODO

## Tricks with `quote` and Lazy Evaluation


(put ++ nil)
(put foo nil)


(def-function ++ (x) (set (quote x) (+ x 1)))

(def foo 3)

foo

(++ foo)

foo

