# HTTP Server as REPL

Part of FPL is a HTTP server based REPL (read evaluate print loop).
It is started by the main method of the class `de.codecentric.fpl.io.HttpServerMain`. 
The main method has to be started with three arguments:

1. The HTTP server port (e.g. 8080)
2. A username for basic authentication
3. A password for basic authentication

All HTTP traffic to the server is protected by the given username/password with basic authentication. 

Be careful with the username and password: Through the Java bridge, FPL can call Java code, which opens a bunch of different ways to break into your computer!

# Using the Server as REPL

Sending a POST request to the URL /fpl will evaluate all given expressions. When you add the query parameter
`lastBlockOnly`, only the last block (paragraph) will be executed. This is useful when you want to
collect a whole "session" in Postman, but want to enter it one expression after the other. Just put an
empty line between the expressions to execute only one expression per call.

# Terminate the Server

There are two ways to terminate the server (except from killing the Java process):

1. Call the function `(stop-server)`.
2. Do a GET request to /fpl/terminate.

