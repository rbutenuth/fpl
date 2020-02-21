# Dictionaries and Related Functions

## Introduction

There are two builtin data structures in FPL: Immutable lists and mutable dictonaries. 
The syntax for a dictionary is similar to a JSON object: 

```
{
   key: 42
   "another-key": bar
}
```

What are the differences? 
- Keys can be symbols or strings
- There is no comma (,) to separate the key value pairs

The values are _not_ evaluated. So you can place a list or a symbol into a dictionary without quoting.

There can't be nil values in a dictionary: When you put a nil value, the mapping for that key is simply
removed from the dictionaries. 

## Functions

### dict-def

Define a value in the scope of an object or dictionary, symbol can be a symbol or a string,
returns the value associated with the symbol/key, original mapping must be nil.

Example:
 
```
(dict-def dictionary symbol 42)
```

### dict-get

Get a value from the scope of an object or dictionary, symbol can be a symbol or a string.

Example: 

```
(dict-get dictionary symbol)
```

### dict-put

Put a value into the scope of an object or dictionary, symbol can be a symbol or a string,
returns the old value associated with the symbol/key. When you want to remove a mapping, 
set the value nil.

Example: 

```
(dict-put dictionary symbol value)
```

### dict-set

Change a value into the scope of an object or dictionary, symbol can be a symbol or a string,
returns the old value associated with the symbol/key, new and old value must not be nil.

Example: 

```
(dict-set dictionary symbol value)
```

