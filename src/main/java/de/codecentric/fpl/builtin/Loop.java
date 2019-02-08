package de.codecentric.fpl.builtin;

import de.codecentric.fpl.data.Scope;
import de.codecentric.fpl.data.ScopeException;

/**
 * Loop functions.
 */
public class Loop {

    /**
     * @param scope Scope to which functions should be added.
     * @throws ScopeException Should not happen on initialization.
     */
    public static void put(Scope scope) throws ScopeException {

        // (while condition expression...)
//        scope.putFunction(new Function("while", "condition", "code...") {
//            @Override
//            public FplObject call(Scope scope, FplObject[] parameters) throws EvaluationException {
//                FplObject result = null;
//                while (evaluateToBoolean(scope, parameters[0])) {
//                    for (int i = 1; i < parameters.length; i++) {
//                        result = parameters[i].evaluate(scope);
//                    }
//                }
//                return result;
//            }
//        });

        // (for-each i (list 1 2 3) (print i))
//        scope.put("for-each", new Function("for-each", "element-name", "list", "code...") {
//            @Override
//            public FplObject call(Scope scope, FplObject[] parameters) throws EvaluationException {
//                String name = evaluateToSymbol(scope, parameters[0]).getName();
//                FplList list = evaluateToList(scope, parameters[1]);
//                Scope loopScope = new Scope(scope);
//                FplObject result = null;
//                for (FplObject value : list) {
//                    loopScope.put(name, value);
//                    for (int i = 2; i < parameters.length; i++) {
//                        result = parameters[i].evaluate(loopScope);
//                    }
//                }
//                return result;
//            }
//        });

    }
    /*
     * from http://www.ai.sri.com/~pkarp/loop.html: (loop for x in '(a b c d e) do (print x) ) (loop for x in '(a b c d e) for y
     * in '(1 2 3 4 5) collect (list x y) ) ((A 1) (B 2) (C 3) (D 4) (E 5)) Emacs-Lisp: (let (myList foundFlag-p i) (setq myList
     * [0 1 2 3 4 5] ) (setq foundFlag-p nil ) (setq i 0) (while (and (not foundFlag-p) (<= i (length myList))) ;; if found, set
     * foundFlag-p (when (equal (elt myList i) 3) (setq foundFlag-p t ) ) (message "value: %s" i) (setq i (1+ i)) ) ) Emacs Lisp
     * Tutorial: http://www.cs.tut.fi/lintula/manual/elisp/emacs-lisp-intro-1.05/emacs-lisp-intro_4.html (let ((variable value)
     * (variable value) ...) body...) Vector: Constant access time, no add/remove
     * http://ergoemacs.org/emacs/elisp_list_vector.html ;; creating a vector (setq v (vector 3 4 5)) ; each element will be
     * evaluated (setq v [3 4 5]) ; each element will NOT be evaluated HashTable:
     * http://ergoemacs.org/emacs/elisp_hash_table.html ;; create a hash table (setq myHash (make-hash-table :test 'equal)) ;;
     * add entries (puthash "joe" "19" myHash) (puthash "jane" "20" myHash) (puthash "carrie" "17" myHash) (puthash "liz" "21"
     * myHash) ;; modify a entry's value (puthash "jane" "16" myHash) ;; remove a entry (remhash "liz" myHash) ;; get a entry's
     * value (setq val (gethash "jane" myHash)) (message val) ; print it
     */
}
