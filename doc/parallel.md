# Functions for Parallelism

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



