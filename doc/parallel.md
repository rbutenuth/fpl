# Functions for Parallelism

### thread-pool-size
Create a new thread-pool with the given size. If never called, FPL uses `ForkJoinPool.commonPool()`.
All running threads stay in the old pool. Returns the old pool size.
```
(thread-pool-size size)
```

### parallel
Evaluate the parameters (`code`) in parallel and return a list with the evaluation results.
```
(parallel code...)
```

This can be used to implement fork-join algorithms, e.g. to compute Fibonacci numbers:

```
(def-function par-fib (n)
	(if-else (le n 2)
		1
		(reduce 
			(lambda (acc value) (+ acc value)) 
			0 
			(parallel (par-fib (- n 1)) (par-fib (- n 2)))
		)
	)
)
```

The two predecessor numbers are computed in parallel, the result is stored in a list.
The `reduce` on the list adds the two numbers. (I know: It's far more efficient to
compute Fibonacci numbers without recursion, this is just an example.)

### parallel-for-each
Apply a function parallel to all list elements, return last result
```
(parallel-for-each function list)
```

### parallel-map
Apply a function parallel to all list elements and return list with applied elements
```
(parallel-map function list)
```

### create-future
Execute the `code` parallel to the current thread. Returns a function (future). Invoking that
function will wait for the result of the code running in parallel. 
```
(create-future code)
```
Example:
```
(put future (create-future ((java-class "java.lang.Thread") sleep 5000)))
(future)
```
Creates a future which will wait for 5 seconds, then wait until it completes. Between that
two lines, you could execute other code in parallel with the code spawned by `create-future`.

### synchronized
Evaluate the parameters, return value of last parameter. This happens within a `synchronized` controlled
by the `monitor`. Be what you choose as monitor. Some values (e.g. small integers, empty list) may be
implemented as singletons, so you may end up with the same instance even when you think you create a new monitor.
Objects (dictionaries) and strings are safe regarding this aspect.

```
(synchronized monitor elements...)
```
