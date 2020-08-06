# Control Structures

## Introduction

### What is true?

Conditional execution of function must have an understanding of truth. FPL has no explicit boolean
value (you may know this from C). In FPL nearly everything is true, false is:
* `nil`
* An empty list
* The integer 0
* The double value 0.0 (be careful, rounding may fool you)
* The empty string



### for-each
Apply a lambda to all list elements, return evaluation result of last lambda.
```
(for-each function list)
```

### if
Evaluate condition, if true, return evaluated if-part, otherwise return nil.
```
(if condition if-part)
```

### if-else
Evaluate condition, if true, return evaluated if-part, otherwise evaluated else-part.
```
(if-else condition if-part else-part)
```

### while
Execute code while condition returns true. Return value is the value of the last evaluated expression.
`code` can be one or more expressions. 
```
(while condition code...)
```
Example:
```
(def counter 10)
(while (ge counter 0) 
	(println counter)
	(set counter (- counter 1))
)
```
The code defines a symbol with value 10. The loop is executed while the value of the symbol `counter` is >= 0. 
So the the lines from 10 to 0 are printed.  The last `set` assigns the value -1. The function `set` returns the 
value of the symbol before changing it, 0 in this case. As a consequence, the return value of the loop is 0.

### sequential
Evaluate the parameters, return value of last parameter.
This can be used to group several expressions where only one is allowed, e.g. the "if" or "else" part.
```
(sequential elements...)
```

### throw
Throw an exception.
```
(throw message)
```

### try-catch
Evaluate the given `expression` and return the result.
In case of an exception, call `catch-function` and return its result. The function is called
with two parameters:
1. The exception message
2. A List with the stack trace. Each element is a list with three elements: Source name, line number, function name.
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
(try-with ((a (open "a) (lambda (x) (close x))) 
            (b (open "b") (lambda (x) (close x))) 
          ) (sequential (put-global "a-in-code" a) (put-global "b-in-code" b) (throw "bam")) (lambda (message stacktrace) (put-global "message" message) 42))");
```
Opens two resources with some open function, the result is stored in the local scope in `a` and `b`. Then the sequential block is executed, which stores the
resource in open state in two global variables, before throwing an exceptions. The `catch-function` stores the exception message in a global variable and
returns 42. So the whole example will return 42 from the `catch-function`.
