# Built-in Functions

## Introduction

Built-in funcions - like all functions in fpl - can have a fixed or variable number of arguments. 
In all variable argument list functions the last parameter name is followed by three dots (`...`). 

## Alphabetically Sorted List of Functions

### %
Modulo of first value by following values.
```
(% op1 op2 ops...)
```

### *
Multiply values.
```
(* op1 op2 ops...)
```

### **
Exponentiation of first value by following values.
```
(** op1 op2 ops...)
```

### +
Add values.
```
(+ op1 op2 ops...)
```

### -
Unary minus or subtract from first.
```
(- ops...)
```

### /
Divide first value by following values.
```
(/ op1 op2 ops...)
```

### add-end
Return a new list with expression added at the end of the given list.
```
(add-end list expression)
```

### add-front
Return a new list with expression added in front of the given list.
```
(add-front expression list)
```

### and
Logic and of parameters.
```
(and expression)
```

### append
Append two lists.
```
(append list-a list-b)
```

### class
Create a new scope and execute the given code within it.
```
(class code...)
```

### def
Assign value in current scope, it must be unassigned before. nil as value not allowed
```
(def symbol value)
```

### def-class
Create a new scope and execute the given code within it. Assign the resulting class to "name"
```
(def-class name code...)
```

### def-field
Assign value in the next object scope, it must be unassigned before. nil as value not allowed
```
(def-field symbol value)
```

### def-function
Define a function.
```
(def-function name parameter-list code...)
```

### def-global
Assign value in global scope, it must be unassigned before. nil as value not allowed
```
(def-global symbol value)
```

### def-sub-class
TODO
```
(def-sub-class name parent code...)
```

### describe
Create a comment in markdown format for a function
```
(describe expression)
```

### dict-def
Define a value in the scope of an object or dictionary,
symbol can be a symbol or a string,
returns the value associated with the symbol/key, original mapping must be nil.
```
(dict-def dict symbol value)
```

### dict-get
Get a value from the scope of an object or dictionary,
symbol can be a symbol or a string.
```
(dict-get dict symbol)
```

### dict-put
Put a value into the scope of an object or dictionary,
symbol can be a symbol or a string,
returns the old value associated with the symbol/key.
```
(dict-put dict symbol value)
```

### dict-set
Change a value into the scope of an object or dictionary,
symbol can be a symbol or a string,
returns the old value associated with the symbol/key, new and old value must not be nil.
```
(dict-set dict symbol value)
```

### eq
Compare for equal.
```
(eq left right)
```

### eval
Evaluate expression.
```
(eval expression)
```

### filter
Filter a list elements.
```
(filter func list)
```

### first
Return first element of the list.
```
(first list)
```

### for-each
Apply a lambda to all list elements, return last result
```
(for-each function list)
```

### ge
Compare left greater or equal than right.
```
(ge left right)
```

### get-element
Return the element at position pos (counted from 0 ) from the given list.
```
(get-element list pos)
```

### gt
Compare left greater than right.
```
(gt left right)
```

### if
Evaluate condition, if true, return evaluated if-part, otherwise nil.
```
(if condition if-part)
```

### if-else
Evaluate condition, if true, return evaluated if-part, otherwise evaluated else-part.
```
(if-else condition if-part else-part)
```

### is-double
Is expression a double?
```
(is-double expression)
```

### is-function
Is expression a function?
```
(is-function expression)
```

### is-integer
Is expression an integer?
```
(is-integer expression)
```

### is-list
Is expression a list?
```
(is-list expression)
```

### is-symbol
Is expression a symbol?
```
(is-symbol expression)
```

### java-class
Create an instance of a Java wrapper object.
```
(java-class class)
```

### java-instance
Create an instance of a Java wrapper object.
```
(java-instance class...)
```

### lambda
Create an anonymous function.
```
(lambda parameter-list code...)
```

### last
Return last element of the list.
```
(last list)
```

### le
Compare lest less or equal than right.
```
(le left right)
```

### list
Make a list out of the parameters.
```
(list element)
```

### lower-half
Return the lower half of a list (opposite to upper-half).
```
(lower-half list)
```

### lt
Compare left less than right.
```
(lt left right)
```

### map
Apply a lambda to all list elements and return list with applied elements
```
(map function list)
```

### ne
Compare for not equal.
```
(ne left right)
```

### new-instance
Create an instance of an object.
```
(new-instance key-value-pair...)
```

### not
Logic not of parameter.
```
(not expression)
```

### or
Logic or of parameters.
```
(or expression)
```

### print
Print parameters.
```
(print expression)
```

### println
Print parameters, followed by line break.
```
(println expression)
```

### put
Assign symbol to evluated value in current scope, deletes if value is null
```
(put symbol value)
```

### put-global
Assign symbol to evluated value in global scope, deletes if value is null
```
(put-global symbol value)
```

### quote
Don't evaluate the argument, return it as is.
```
(quote expression)
```

### remove-first
Return list without the first element.
```
(remove-first list)
```

### remove-last
Return list without the last element.
```
(remove-last list)
```

### set
Reassign value in scope chain. nil as value not allowed
```
(set symbol value)
```

### size
Number of elements in a list.
```
(size list)
```

### sub-class
Create a new scope and execute the given code within it, set parent to parameter.
```
(sub-class parent code...)
```

### sub-list
Return a part from the given list, including start, excluding end (counted from 0).
```
(sub-list list start end)
```

### this
The next object in the scope chain, can be nil when not within an object context.
```
(this)
```

### type-of
Return type of argument as string
```
(type-of value)
```

### upper-half
Return the upper half of a list (opposite to lower-half).
```
(upper-half list)
```

### while
Execute code while condition returns true.
```
(while condition code...)
```

### xor
Logic xor of parameters.
```
(xor expression)
```

