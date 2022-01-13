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

Keys can be any values (except nil), but only values where there is a meaningful hash code and equals 
method in the backing Java code make sense: string, integer, double.   

## Functions

### dict

Create a new dictionary from key value pairs. The number of parameters must be even, keys must not be nil.

```
(dict pairs...)
```


### sorted-dict

Create a new sorted dictionary from key value pairs.
The lambda sort takes two arguments (left, right) and must return a number:
< 0 if left < right, 0 for left = right and > 0 for left > right.
When sort-lambda is nil, the natural string order is used. This works only when the keys
are string, integer, or double.

```
(sorted dict sort-lambda pairs...)
```

### dict-def

Define a value in the scope of an object or dictionary, key must not be nil,
returns the value associated with the key, original mapping must be nil.

```
(dict-def dictionary key 42)
```

### dict-get

Get a value from the scope of an object or dictionary.

```
(dict-get dictionary key)
```

### dict-put

Put a value into the scope of an object or dictionary, key must not be nil,
returns the old value associated with the key. When you want to remove a mapping, 
set the value nil.

```
(dict-put dictionary key value)
```

### dict-set

Change a value into the scope of an object or dictionary, key must be not nil,
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
(dict-entries dictionary)
```

### dict-size

The number of mappings in the dictionary.

```
(dict-size dictionary)
```

### dict-peek-first-key
Returns the first key (random for unsorted dictionaries) of a dictionary.

```
(dict-peek-first-key)
```

### dict-fetch-first-key
Returns and removes the first key (random for unsorted dictionaries) of a dictionary.

```
(dict-fetch-first-key)
```

### dict-fetch-last-key
Returns and removes the last key (random for unsorted dictionaries) of a dictionary.

```
(dict-fetch-last-key)
```

### dict-fetch-first-value
Returns and removes the first value (random for unsorted dictionaries) of a dictionary.

```
(dict-fetch-first-value)
```

### dict-fetch-last-value
Returns and removes the last value (random for unsorted dictionaries) of a dictionary.

```
(dict-fetch-last-value)
```

### dict-fetch-first-entry
Returns and removes the first entry (random for unsorted dictionaries) of a dictionary.
The entry is a list of key and value.

```
(dict-fetch-first-entry)
```

### dict-fetch-last-entry
Returns and removes the last entry (random for unsorted dictionaries) of a dictionary.
The entry is a list of key and value.

```
(dict-fetch-last-entry)
```

