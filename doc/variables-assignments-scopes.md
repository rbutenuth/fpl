# Definitions, Assignments, and Scopes

Even when you design an untyped language, you can protect against some errors.
For this reasons fpl distinguishes between `def`, `set`, and `put`.

For example, `(def x 42)` associates the symbol `x` with the value `42` in the current scope. 
This works only when `x` is not associated with any value before, otherwise you get the error message
`Duplicate key: x`. This avoids to overwrite a variable by accident.

Whenever you want to change the value of an existing variable, you should use `set`. This works on existing variables only. 
When you try `(set foo "bar")` without defining `foo` before, the error message will be 
`No value with key foo found`.   


TODO: put, def-global, def-function etc.

(put ++ nil)
(put foo nil)
(put square nil)

(def-function square (x) * x x)

(def-function ++ (x) (set (quote x) (+ x 1)))

(def foo 3)

foo

(++ foo)

foo

(list nil nil nil)


(put square nil)
(def-function square (x) * x x)
