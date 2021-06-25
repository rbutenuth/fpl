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

### char-at
Return the code (integer) of the character at position index.
```
(char-at string index)
```

### from-chars
Build a string from a list of characters (integers).
```
(from-chars list-of-chars)
```

### index-of
Determine the first index of pattern in a string. Return -1 for not found.
```
(index-of string pattern)
```

### last-index-of
Determine the last index of pattern in a string. Return -1 for not found.
```
(last-index-of string pattern)
```

### substring
Returns a substring starting at begin-index (including) and ending at end-index (excluding).
```
(substring begin-index end-index)
```

### match
Matches a string against a regular expression. Returns a list where the first element contains the position of the match, 
followed by the matches. The second entry in the list
is the complete match, followed by the partial matches (marked by parentheses in the pattern). Empty list
if no match found.
```
(match string regex)
```

### replace-all
Replaces each substring of this string that matches the given regex with the given replacement.
(See String.replaceAll for more details.)
```
(replace-all string regex replacement)
```

### to-lower-case
Convert the string to lower case.
```
(to-lower-case string)
```

### to-upper-case
Convert the string to upper case.
```
(to-upper-case string)
```

### symbol
Create a symbol.
```
(symbol string)
```

### name-of-symbol
Determine the name of a symbol.
```
(name-of-symbol symbol)
```

### serialize-to-json
Convert value to JSON string. When the value contains symbols, `true` and `false` will be converted to
JSON boolean values. All other symbols are converted to JSON strings.
```
(serialize-to-json value)
```

### parse-json
Convert a JSON string to list/object.
`true` and `false` are converted to 1 and 0.
```
(parse-json string)
```
