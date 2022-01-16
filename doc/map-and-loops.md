# Map and Loop functions

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

Or an efficient way to compute Fibonacci numbers: 
```
(def-function fib (n)
	(if-else (le n 2)
		1
		(sequential 
			(def fib-2 1)
			(def fib-1 1)
			(def i 3)
			(def fib 0)
			(while (le i n)
				(set fib (+ fib-2 fib-1))
				(set fib-2 fib-1)
				(set fib-1 fib)
				(set i (+ i 1))
			)
			fib
		)
	)
)
```


The code defines a symbol with value 10. The loop is executed while the value of the symbol `counter` is >= 0. 
So the the lines from 10 to 0 are printed.  The last `set` assigns the value -1. The function `set` returns the 
value of the symbol before changing it, 0 in this case. As a consequence, the return value of the loop is 0.

### for-each
Apply a lambda to all list elements, return evaluation result of last lambda.
```
(for-each lambda list)
```

### from-to-inclusive
Apply a lambda to all numbers from start (inclusive) to end (inclusive). Start and end must be numbers.
End may be smaller then start, in this case to sequence of numbers is decreasing.
The lambda must accept one parameter, the current number.
Result is the result of the last lambda evaluation.
```
(from-to-inclusive lambda start end)
```

### from-to
Apply a lambda to all numbers from start (inclusive) to end (exclusive). Start and end must be numbers.
End may be smaller then start, in this case to sequence of numbers is decreasing.
The lambda must accept one parameter, the current number.
Result is the result of the last lambda evaluation.
```
(from-to lambda start end)
```

### map-sequence
Apply a lambda to all numbers from start (inclusive) to end (exclusive). Start and end must be numbers.
End must not be less than start.
The lambda must accept one parameter, the current number.
Result is a list of the applied lambda for all the numbers in the sequence.
```
(map-sequence lambda start end)
```

### reduce-sequence
Reduce a sequence of numbers from `start` (inclusive) to `end` (exclusive) to one value. The lambda must accept two parameters: 
`accumulator` and `index` (starting at 0). It must return the "reduction" of accumulator and value.
```
(reduce-sequence lambda accumulator start end)
```

## Loops over lists

### for-each
Apply a lambda to all list elements, return last result.
The lambda receives two parameters: List element, index (starting from 0).
```
(for-each lambda some-list)
```

### map
Apply a lambda to all list elements and return list with applied elements
The lambda receives two parameters: List element, index (starting from 0).
```
(map lambda some-list)
```

### flat-map
Apply a lambda to all list elements, the result of the lambda must be a list. Return list with applied elements of all returned lists.
The lambda receives two parameters: List element, index (starting from 0).

```
(flat-map lambda some-list)
```

### filter
Filter a list elements. Return a list containing all elements from input list for which `lambda` returned true.
The lambda receives two parameters: List element, index (starting from 0).
```
(filter lambda some-list)
```

### reduce
Reduce a list to one value. The lambda must accept three parameters: 
`accumulator`, `value`, and `index` (starting at 0). It must return the \"reduction\" of accumulator and value.
```
(reduce lambda accumulator some-list)
```
Example:
```
(reduce (lambda (acc value index) (+ acc value)) 0 '(1 2 3 4 5 6))
```
Computes the sum of the number 1 to 6.

### combine
Take two lists as input, call a lambda with three parameters (element from first, second list, index)
and return a list with the result of this lambda. In case the lists have different
length, stop when the shorter list ends.

```
(combine lambda list-1 list-2)
```

### split-by
Split a list into a list of several lists. Each time the lambda returns true, a new list is started.
The lambda is called with two arguments: A list element and a counter (starting at 0). The result
of the lambda for the call of the first list element is ignored. 

```
(split-by lambda list)
```

### group-by
Convert a list in a dictionary of lists. The key is the result of the lambda, converted to a string.
The lambda is called with two arguments: A lit element and a counter (starting at 0). When the 
result of the lambda is nil, the corresponding element is ignored.

```
(group-by lambda list)
```

### map-to-dict
Apply a lambda to all list elements and return a dictionary. The dictionary is build from the results
of the lambdas. The first must return the key as string, the second a value (any type). 
When the key is nil, the second lambda is not called and nothing is put to the dictionary.
Adding to the dictionary is done by put, so mappings may overwrite each other or even remove mappings,
when value is nil.

The first lambda receives a list element as parameter and the index (starting from 0).
The second lambda receives three parameters: The first is the previous value contained in the dictionary for the given
key (may be nil if no mapping exists), the second the list element to be mapped. The third the index (starting from 0).
```
(map-to-dict key-lambda value-lambda some-list)
```

### map-to-sorted-dict
Apply a lambda to all list elements and return a sorted dictionary. The dictionary is build from the results
of the lambdas. The first must return the key as string, the second a value (any type). 
When the key is nil, the second lambda is not called and nothing is put to the dictionary.
Adding to the dictionary is done by put, so mappings may overwrite each other or even remove mappings,
when value is nil.
The first lambda receives a list element as parameter and the index (starting from 0).
The second lambda receives three parameters: The first is the previous value contained in the dictionary for the given
key (may be nil if no mapping exists), the second the list element to be mapped, the third the index (starting from 0).
The third lambda controls the sorting of the dictionary. It takes two arguments (left, right) and must return a number:
< 0 if left < right, 0 for left = right and > 0 for left > right. When the thirs lambda is nil, natural string ordering
is used. 
```
(map-to-sorted key-lambda value-lambda sort-lambda some-list)
```

