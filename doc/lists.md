# Lists

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

### append
Append two lists, return the concatenation of the two lists as one.

```
(append list-a list-b)
```

### filter
Filter a list elements. Return a list containing all elements from input list for which func returned true.
```
(filter func list)
```

### first
Return first element of the list.
```
(first list)
```

### last
Return last element of the list.
```
(last list)
```

### for-each
Apply a lambda to all list elements, return last result
```
(for-each function list)
```

### get-element
Return the element at position pos (counted from 0 ) from the given list.
```
(get-element list pos)
```

### is-list
Is expression a list?
```
(is-list expression)
```

### list
Make a list out of the parameters.
```
(list element)
```

### lower-half
Return the lower half of a list (opposite to upper-half). In case the number of elements is not even,
the lower half has one element less than the upper half.
```
(lower-half list)
```
Example:
```
(lower-half (list 1 2 3))
```
returns
```
(1)
```

### upper-half
Return the upper half of a list (opposite to lower-half). In case the number of elements is not even,
the lower half has one element less than the upper half.
```
(upper-half list)
```
Example:
```
(upper-half (list 1 2 3))
```
returns
```
(2 3)
```

### map
Apply a lambda to all list elements and return list with applied elements
```
(map function list)
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

### size
Number of elements in a list.
```
(size list)
```

### sub-list
Return a part from the given list, including start, excluding end (counted from 0).
```
(sub-list list start end)
```

