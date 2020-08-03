# Conditions and Loops

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

### throw
Throw an exception.
```
(throw message)
```
