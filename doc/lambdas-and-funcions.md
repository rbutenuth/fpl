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

## Dynamic definiton of lambdas and functions

The two functions `lambda` and `def-function` are a little bit special: They only evaluate the parameter
with the argument list, when it is not already a list. This is for convenience: This way you don't have to qoute
the list of parameter names. The same holds for the code: You don't need to quote it. The downside: You are in trouble
when you want to build code at runtime and make from it. Therefore, there are two special variants:

```
(lambda-dynamic parameter-list code-list)
```

Evaluates the first argument, the result must be a list of symbols and/or strings. The second parameter is evaluated
to the code which has to be executed in the functions.

```
(def-function-dynamic name args code)
```

Like `lambda-dynamic`, but assigns the function to the symbol to which `name` evaluates.

## Lazy Evaluation

When calling a function, you can pass parameters. Parameters can be any expression, for example a 
constant value (e.g. 42), a symbol (e.g. `foo`), or a function call. The expression is passed to the
function "as is", together with the scope where it occurs. It is _not_ evaluated on calling the function,
the evaluation is delayed until its value is really needed.   

So the parameter may never be evaluated. Have a look at the following example:

```
(def-function print-and-return (n)
	(println "n:" n)
	n
)
;
(def-function not-all-used (a b c)
	(+ a b)
)
;
(not-all-used 
	(print-and-return 5)
	(print-and-return 6)
	(print-and-return 7)
)
```

The function `not-all-used` is called with three parameters, but only the first two are used. So
the third will never be evaluated and only the values 5 and 6 will be printed. 

This may look like a minor detail, but it is essential: Without this, functions like `if` and `if-else`
would not be possible. Let's see another example:

```
(def-function factorial (n)
   (if-else (le n 1)
       1
       (* n (factorial (- n 1)))
   )
)
```

Computing the `if` and the `else` part and then deciding wich one will be returned would result in an
endless recursion. Lazy evaluation makes this function possible!

## Optional Arguments and Variable Argument Lists

Functions with a variable number of arguments work similar as in Java: Just append a `...` to the last 
parameter name, example:

```
(def-function varargs (a b...)
	(println "a:" (type-of a) a)
	(println "b:" (type-of b) b)
)
```

This defines a function where `a` is the first parameter, followed by an arbitrary number arguments
(including null). These are assigned as a list to `b`. So the call 

```
(varargs 3 4 5)
```
 
Creates the following output:

```
a: integer 3
b: list (4 5)
```


## Currying

What happens when you call a function with less parameters then specified in the function definition? In most
programming languages the result would be an error or the remaining parameters would be set to `null`. In
FPL, you get a new function as result, which is combined from the given parameters and the initial function.
You can call it with the missing parameters. Let's see an example:

```
(def-function plus (a b)
	(+ a b)
)

(def plus3 (plus 3))


(plus3 1)
```

`plus` takes two parameters, when called with only one, it will result in a new function with one parameter.
It will add the number given in the first call to the parameter. So here `plus3` is the same as

```
(def-function plus3 ( b)
	(+ 3 b)
)
```

It was introduced by Gottlob Frege, developed by Moses Schönfinkel, and further developed by Haskell Curry.
So another name for this technique is 'Schönfinkeln'. See [Wikipedia](https://en.wikipedia.org/wiki/Currying) for more details.

## Tricks with `quote` and Lazy Evaluation

Lazy evaluation allows some crazy tricks, here an example of the implementiation of `++`, as known from C and Java:

```
(def-function ++ (x) (set (quote x) (+ x 1)))
```

Defining `foo` as `(def foo 3)` and calling `(++ foo)` increements `foo` by one, so the new value is 4.

Why this works is left as an excercise for the reader. :-)
