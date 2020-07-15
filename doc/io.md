# I/O Functions

### parse-string
Parse or evaluate all expressions within the string. Return a list which contains the results.
```
(parse-string string evaluate)
```

### parse-resource
Parse or evaluate all expressions within the resource given by the URI. Return a list which contains the results.
The resource must be UTF-8 encoded. 
```
(parse-resource uri evaluate)
```

### to-string
Converts an expression into its string representation.
```
(to-string expression)
```

### write-to-file
Write the content of a string to a file. Use UTF-8 as encoding.
```
(write-string-to-file filename content)
```

### http-request
Do an HTTP-request. Input parameters:
* url URL consisting of protocol, host, (optional port), path
* method GET, POST, PUT, DELETE, PATCH
* headers A map of headers. Values can be single values (strings) or list of strings
* query-params A map of query-params. Values can be single values (strings) or list of strings
* body A string with the body
* user User for basic authentication (may be nil)
* password Password for basic authentication (may be nil)
The result is a list with
* status code
* response headers (map with single values or list of values)
* body
```
(http-request url method headers query-params body user password)
```
