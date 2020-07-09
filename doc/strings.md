# String functions

### join
Evaluate all expressions, convert them to strings, join (concatenate) the strings.
```
(join expression...)
```

### format-number
Format a number to string format. The format is a Java DecimalFormat string. The locale a two letter locale.
```
(format-number format locale number)
```

### parse-number
Parse a string to a number. The format is a Java DecimalFormat string. The locale a two letter locale.
```
(parse-number format locale string)
```

### length
Determine the length (number of characters) of a string
```
(length string)
```

