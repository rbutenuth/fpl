# Java Wrapper

The Java wrapper functions allow to instantiate Java objects, get a handle for the class itself, and to call methods.
These can be static methods on a class handle or instance methods on objects.

### java-class
Create a handle for a Java class. This can be used to call static methods.
```
(java-class class)
```
Example:
```
((java-class "java.lang.System") currentTimeMillis)
```
This code create a handle to `System` and calls the static method `currentTimeMillis`.


### java-instance
Create an instance of a Java wrapper object.
```
(java-instance class...)
```
`class` can be a symbol or an expression that evaluates to a string. The string has to be the fully qualified name of a class.
The remaining parameters are taken as arguments for a constructor of that class. The constructor is called, then the return value 
is wrapped as an FPL value.

The result is a function. When called, the first parameter is used to determine a method of the class. This method is called 
with the remaining parameters as method parameters. Example:
```
(def array-list (java-instance "java.util.ArrayList"))
(array-list add 5)
(array-list add 6)
(array-list get 1)
```
This code create an ArrayList, assigns it to `array-list`, adds the values 5 and 6, then calls the method `get` to retrieve
the element at position 1, which is 6.

 
