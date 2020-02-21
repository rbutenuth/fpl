# Objects and Classes

## Introduction

### class
Create a new scope and execute the given code within it.
```
(class code...)
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

### def-sub-class
TODO
```
(def-sub-class name parent code...)
```

### new-instance
Create an instance of an object.
```
(new-instance key-value-pair...)
```

### sub-class
Create a new scope and execute the given code within it, set parent to parameter.
```
(sub-class parent code...)
```

### this
The next object in the scope chain, can be nil when not within an object context.
```
(this)
```
