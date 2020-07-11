# I/O Functions

### parse-resource
Parse or evaluate all expressions within the resource given by the URI. Return a list which contains the results.
The resource must be UTF-8 encoded. 
```
(parse-resource uri evaluate)
```

### parse-string
Parse or evaluate all expressions within the string. Return a list which contains the results.
```
(parse-string string evaluate)
```

### write-to-file
Write the content of a string to a file. Use UTF-8 as encoding.
```
(write-string-to-file filename content)
```
