# Arithmetic and Comparison

## Arithmetic functions

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

### *
Multiply values.
```
(* op1 op2 ops...)
```

### /
Divide first value by following values.
```
(/ op1 op2 ops...)
```

### %
Modulo of first value by following values.
```
(% op1 op2 ops...)
```

### **
Exponentiation of first value by following values.
```
(** op1 op2 ops...)
```

## Comparison functions

### eq
Compare for equal.
```
(eq left right)
```

### ne
Compare for not equal.
```
(ne left right)
```

### ge
Compare left greater or equal than right.
```
(ge left right)
```

### gt
Compare left greater than right.
```
(gt left right)
```

### le
Compare lest less or equal than right.
```
(le left right)
```

### lt
Compare left less than right.
```
(lt left right)
```

# Logical functions

### not
Logic not of parameter.
```
(not expression)
```

### and
Logic and of parameters.
```
(and op1...)
```

### or
Logic or of parameters.
```
(or op1...)
```

### xor
Logic xor of parameters.
```
(xor op1...)
```

## Type checking functions

### is-double
Is expression a double?
```
(is-double expression)
```

### is-integer
Is expression an integer?
```
(is-integer expression)
```

### is-number
Is expression an integer or double?
```
(is-number expression)
```

### is-list
Is expression a list?
```
(is-list expression)
```

### is-string
Is expression a string?
```
(is-string expression)
```

### is-function
Is expression a function?
```
(is-function expression)
```

### is-object
Is expression an object?
```
(is-object expression)
```

### is-symbol
Is expression a symbol?
```
(is-symbol expression)
```

## Conversion

### round
Round a double to a integer. `nil` is converted to 0.
```
(round double-value)
```

### to-integer
Cast (truncate) a double to a integer. `nil` is converted to 0.
```
(to-integer double-value)
```
