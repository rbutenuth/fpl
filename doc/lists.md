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


## Loops over lists

### for-each
Apply a lambda to all list elements, return last result
```
(for-each lambda some-list)
```

### map
Apply a lambda to all list elements and return list with applied elements
```
(map lambda some-list)
```

### map-to-dict
Apply a lambda to all list elements and return a dictionary. The dictionary is build from the results
of the lambdas. The first must return the key as string, the second a value (any type). 
When the key is an empty string, the second lambda is not called and nothing is put to the dictionary.
Adding to the dictionary is done by put, so mappings may overwrite each other or even remove mappings,
when value is nil.
The first lambda receives a list element as parameter.
The second lambda receives two parameters: The first is the previous value contained in the dictionary for the given
key (may be nil if no mapping exists), the second the list element to be mapped.
```
(map-to-dict key-lambda value-lambda some-list)
```

### flat-map
Apply a lambda to all list elements, the result of the lambda must be a list. Return list with applied elements of all returned lists.
```
(flat-map lambda some-list)
```

### filter
Filter a list elements. Return a list containing all elements from input list for which `lambda` returned true.
```
(filter lambda some-list)
```

### reduce
Reduce a list to one value. The lambda must accept two parameters: 
`accumulator` and `value`. It must return the \"reduction\" of accumulator and value.
```
(reduce lambda accumulator some-list)
```
Example:
```
(reduce (lambda (acc value) (+ acc value)) 0 '(1 2 3 4 5 6))
```
Computes the sum of the number 1 to 6.

