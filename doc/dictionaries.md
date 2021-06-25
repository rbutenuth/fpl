# Dictionaries and Related Functions

## Introduction

There are two builtin data structures in FPL: Immutable lists and mutable dictonaries. 
A dictionary is created with the function `dict`  

```
(dict
   "key" 42
   "another-key" bar
)
```

There can't be nil values in a dictionary: When you put a nil value, the mapping for that key is simply
removed from the dictionary. 

## Functions

### dict

Create a new dictionary from key value pairs. The number of parameters must be even, keys must be strings.

```
(dict pairs...)
```

### dict-def

Define a value in the scope of an object or dictionary, key must be a string,
returns the value associated with the key, original mapping must be nil.

```
(dict-def dictionary key 42)
```

### dict-get

Get a value from the scope of an object or dictionary, key must be a string,

```
(dict-get dictionary key)
```

### dict-put

Put a value into the scope of an object or dictionary, key must be a string,
returns the old value associated with the key. When you want to remove a mapping, 
set the value nil.

```
(dict-put dictionary key value)
```

### dict-set

Change a value into the scope of an object or dictionary, key must be a string,
returns the old value associated with the key, new and old value must not be nil.

```
(dict-set dictionary key value)
```

### dict-keys

Get all keys of an object or dictionary as a list.

```
(dict-keys dictionary)
```

### dict-values

Get all values of an object or dictionary as a list.

```
(dict-values dictionary)
```

### dict-entries

Get all entries of an object or dictionary as a list. Each entry is a list with two elements: key and value

```
(dict-entries entries)
```
