# I/O Functions

### read-resource
Read or evaluate all expressions within the resource given by the URI. Return a list which contains the results.
The resource must be UTF-8 encoded. 
```
(read-resource uri evaluate)
```

### write-to-file
Write the content of a string to a file. Use UTF-8 as encoding.
```
(write-to-file filename content)
```
