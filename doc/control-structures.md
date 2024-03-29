# Control Structures

## Introduction

### What is true?

Conditional execution of function must have an understanding of truth. FPL has no explicit boolean
value (you may know this from C). In FPL nearly everything is `true`, `false` is:
* `nil`
* An empty list
* The integer 0
* The double value 0.0 (be careful, rounding may fool you)
* The empty string


### if
Evaluate condition, if `true`, return evaluated if-part, otherwise return `nil`.
```
(if condition if-part)
```

### if-else
Evaluate condition, if `true`, return evaluated if-part, otherwise evaluated else-part.
```
(if-else condition if-part else-part)
```

### cond
Handle condition expression pairs. When the first condition is `true`, the first expression is executed and
the evaluation result is returned. When not, continue with next condition.
When the number of parameters is not even, the last one is evaluated when all conditions are false.
```
(cond condition expression...)
```

### sequential
Evaluate the parameters, return value of last parameter.
This can be used to group several expressions where only one is allowed, e.g. the "if" or "else" part.
```
(sequential elements...)
```

### scope
Evaluate the expressions within a new scope, return value of last expression.
```
(scope expression...)
```

### pipeline
Evaluate the expressions within a new scope, return value of last expression.
The evaluation result of the expressions is bound to the symbol given as parameter `pipe-key`.,
```
(pipeline pipe-key expressions...)
```

Example:
```
(def-function plus-mult (a b)
	(pipeline $
		(+ a b)
		(* $ 10)
	)
)
(plus-mult 3 4)
```

Will return 70 (10 * (3 + 4).

### throw
Throw an exception.
```
(throw message)
```

### throw-with-id
Throw an exception, together with an integer `id` and a `message`
```
(throw-with-id message id)
```

### try-catch
Evaluate the given `expression` and return the result.
In case of an exception, call `catch-function` and return its result. The function is called
with three parameters:
1. The exception message
2. An id (defaults to 0)
3. A List with the stack trace. Each element is a list with three elements: Source name, line number, function name.
When the `catch-function` is `nil`, then the exception is thrown again.
```
(try-catch expression catch-function)
```

### try-with
Open `resources`, evaluate (and return the value of) an `expression`, catch exceptions.  
```
(try-with resources expression catch-function)
```
Example:
```
(try-with ((a (open "a") (lambda (x) (close x))) 
           (b (open "b") (lambda (x) (close x))) 
          )
          (sequential 
            (put-global "a-in-code" a)
            (put-global "b-in-code" b)
            (throw "bam")
          )
          (lambda (message id stacktrace) (put-global "message" message) 42)
) 
```
Opens two resources with some open function, the result is stored in the local scope in `a` and `b`. Then the sequential block is executed, which stores the
resource in open state in two global variables, before throwing an exceptions. The `catch-function` stores the exception message in a global variable and
returns 42. So the whole example will return 42 from the `catch-function`.
