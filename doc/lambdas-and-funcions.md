# Lambdas and Functions

TODO...

## Tricks with `quote` and Lazy Evaluation


(put ++ nil)
(put foo nil)


(def-function ++ (x) (set (quote x) (+ x 1)))

(def foo 3)

foo

(++ foo)

foo

