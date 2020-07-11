# I/O Functions

### read-resource
Parse or evaluate all expressions within the resource given by the URI. Return a list which contains the results.
The resource must be UTF-8 encoded. 
```
(parse-resource uri evaluate)
```

### write-to-file
Write the content of a string to a file. Use UTF-8 as encoding.
```
(write-string-to-file filename content)
```
