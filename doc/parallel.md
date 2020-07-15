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
(put future (create-future ((java-class java.lang.Thread) sleep 5000)))
(future)
```
Creates a future which will wait for 5 seconds, then wait until it completes. Between that
two lines, you could execute other code in parallel with the code spawned by `create-future`.
