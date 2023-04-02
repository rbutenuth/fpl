# Lists

### add-end
Return a new list with expression added at the end of the given list.
```
(add-end some-list expression)
```

### add-front
Return a new list with expression added in front of the given list.
```
(add-front expression some-list)
```

### append
Append two lists, return the concatenation of the two lists as one.

```
(append list-a list-b)
```

### first
Return first element of the list.
```
(first some-list)
```

### last
Return last element of the list.
```
(last some-list)
```

### get-element
Return the element at position pos (counted from 0 ) from the given list.
```
(get-element some-list pos)
```

### set-element
Replace the element at position pos (counted from 0 ) from the given list.
```
(set-element some-list pos element)
```

### patch-elements
Replace some elements from this `list` with elements of another list. The second list
must not have the same number of elements as are removed from the original list.
`from` is the index of the first replaced element, `new-elements` is the list
with the new elements, `num-replaced` elements will be removed.

```
(patch-elements list from new-elements num-replaced)
```

### is-list
Is expression a list?
```
(is-list expression)
```

### list
Make a list out of the parameters.
```
(list element...)
```

### lower-half
Return the lower half of a list (opposite to upper-half). In case the number of elements is not even,
the lower half has one element less than the upper half.
```
(lower-half some-list)
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
(upper-half some-list)
```
Example:
```
(upper-half (list 1 2 3))
```
returns
```
(2 3)
```

### remove-first
Return list without the first element.
```
(remove-first some-list)
```

### remove-last
Return list without the last element.
```
(remove-last some-list)
```

### replace-elements
Replace some elements from this list with elements of another list. It is not required for the second list
to have the same number of elements as are removed from the original list.
`from` is the index of the first replaced element, `new-elements` is the list
with the new elements, `num-replaced` elements will be removed.
```
(replace-elements list from new-elements num-replaced)
```


### size
Number of elements in a list.
```
(size some-list)
```

### is-empty
Is this list empty? (Works also on strings and objects.) `nil` is empty, too.
```
(is-empty some-list)
```

### sub-list
Return a part from the given list, including start, excluding end (counted from 0).
```
(sub-list some-list start end)
```

### sort
Sort a list. The lambda takes two arguments (left, right) and must return a number:
< 0 if left < right, 0 for left = right and > 0 for left > right.
```
(sort lambda some-list)
```

Example:
```
(sort 
	(lambda (a b)
		(if-else (lt a b) 
			-1 
			(if-else (gt a b) 
				1 
				0
			)
		)
	) 
	values
)
```


