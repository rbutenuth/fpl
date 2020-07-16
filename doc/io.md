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
* response headers (map with single values or list of values), header names are converted to lower case.
* body
```
(http-request url method headers query-params body user password)
```

### http-server
Start an HTTP server. Returns a function to terminate the server, parameter is the delay in seconds.
```
(http-server port authenticator logger handlers...)
```
Example for `handlers`:
```
	("GET" "/some-path/*" some-function)
	("POST" "/other-path/" other-function)
```
The `logger`(may be `nil`) is a function which is called when an error in handling a call happens. 
Parameter is the error message.
The `authenticator` must be `nil` or a function. The function receives two parameters:
# user
# password
It must return `true` when the user is valid.

The callback handler functions are called with the following parameters:
* path Complete path, including the prefix defined in `handlers`.
* headers Request headers as map (header names converted to lower case)
* params Request parameters as map
* body Request body as string. May be `nil`
The function must return a list with three elements:
# HTTP status code
# map with response headers
# Body as string, may be `nil`
