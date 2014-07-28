(ns gcc.core-test
  (:use gcc.core
        midje.sweet)
  (:require [clojure.test :refer :all]))

(defn cleanup []
  (swap! lambda-counter (fn [_] 0))
  (swap! branch-counter (fn [_] 0)))

(fact "basics"
      (to-instruction-ast 1 nil nil) => {:result  [["LDC 1"]] :lambdas nil :branches {}}
      (to-instruction-ast '(= 1 0) nil nil) => {:result  [["LDC 1"] ["LDC 0"] ["CEQ"]] :lambdas nil :branches {}}
      (to-instruction-ast '(> 1 0) nil nil) => {:result [["LDC 1"] ["LDC 0"] ["CGT"]] :lambdas nil :branches {}}
      (to-instruction-ast '(car 0) nil nil) => {:result [["LDC 0"] ["CAR"]] :lambdas nil :branches {}}
      (to-instruction-ast '(cons 1 2) nil nil) => {:result [["LDC 1"] ["LDC 2"] ["CONS"]] :lambdas nil :branches {}}
      (to-instruction-ast '(car 0) nil nil) => {:result [["LDC 0"] ["CAR"]] :lambdas nil :branches {}}
      (to-instruction-ast '(cdr 0) nil nil) => {:result [["LDC 0"] ["CDR"]] :lambdas nil :branches {}}
      (to-instruction-ast '(dbg 0) nil nil) => {:result [["LDC 0"] ["DBUG"]] :lambdas nil :branches {}}
      (to-instruction-ast '(brk) nil nil) => {:result [["BRK"]] :lambdas nil :branches {}}
      (to-instruction-ast '(atom? 0) nil nil) => {:result [["LDC 0"] ["ATOM"]] :lambdas nil :branches {}}
      (to-instruction-ast '(cons 1 nil) nil nil) => {:result [["LDC 1"] ["LDC 0"] ["CONS"]] :lambdas nil :branches {}}
      (to-instruction-ast '(cons true nil) nil nil) => {:result [["LDC 1"] ["LDC 0"] ["CONS"]] :lambdas nil :branches {}}
      (to-instruction-ast '(cons false nil) nil nil) => {:result [["LDC 0"] ["LDC 0"] ["CONS"]] :lambdas nil :branches {}}
      (to-instruction-ast '(mktuple 1 2) {} {}) => {:result [["LDC 1"] ["LDC 2"] ["CONS"]] :lambdas {} :branches {}}
      (to-instruction-ast '(mktuple 1 2 3 4) {} {}) => {:result [["LDC 1"] ["LDC 2"] ["LDC 3"] ["LDC 4"] ["CONS"] ["CONS"] ["CONS"]] :lambdas {} :branches {}}
      (to-instruction-ast '(mklist 1) {} {}) => {:result [["LDC 1"] ["LDC 0"] ["CONS"]] :lambdas {} :branches {}}
      (to-instruction-ast '(mklist 1 2 3 4) {} {}) => {:result [["LDC 1"] ["LDC 2"] ["LDC 3"] ["LDC 4"] ["LDC 0"] ["CONS"] ["CONS"] ["CONS"] ["CONS"]] :lambdas {} :branches {}}
      (cleanup))

(fact "lambda body"
      (to-instruction-ast '(lambda (i)
                   (brk)
                   (- i 1))
          nil
          nil) => {:result  [["LDF @$lambda-1"]]
                   :lambdas {"$lambda-1"
                             [["BRK" ["$lambda-1"]]
                              ["LD 0 0"]
                              ["LDC 1"]
                              ["SUB"]
                              ["RTN"]]}
                   :branches {}
                   }
          (cleanup))

(fact "defun body"
      (to-instruction-ast '(defun x (i)
                   (brk)
                   (- i 1))
          nil
          nil) => {:result nil
                   :lambdas {"x"
                             [["BRK" ["x"]]
                              ["LD 0 0"]
                              ["LDC 1"]
                              ["SUB"]
                              ["RTN"]]}
                   :branches {}}
          (cleanup))

(fact "let body - empty bindings"
      (to-instruction-ast '(let ()
             (brk)
             (- 0 1))
          nil
          nil) => {:result  [["LDF @$lambda-1"]]
                   :lambdas {"$lambda-1"
                             [["BRK" ["$lambda-1"]]
                              ["LDC 0"]
                              ["LDC 1"]
                              ["SUB"]
                              ["RTN"]]}
                   :branches {}}
          (cleanup))

(fact "let body - with binding"
      (to-instruction-ast '(let ((i 0))
             (brk)
             (- i 1))
          nil
          nil) => {:result  [["LDC 0"] ["LDF @$lambda-1"] ["AP 1"]]
                   :lambdas {"$lambda-1"
                             [["BRK" ["$lambda-1"]]
                              ["LD 0 0"]
                              ["LDC 1"]
                              ["SUB"]
                              ["RTN"]]}
                   :branches {}}
          (cleanup))

(fact "lambda application"
      (to-instruction-ast '((lambda (i) (- i 1)) 2)
          nil
          nil) => {:result  [["LDC 2"]
                             ["LDF @$lambda-1"]
                             ["AP 1"]]
                   :lambdas {"$lambda-1"
                             [["LD 0 0" ["$lambda-1"]]
                              ["LDC 1"]
                              ["SUB"]
                              ["RTN"]]}
                   :branches {}}
      (cleanup))

(fact "reverse"
      (to-instruction-ast '(defun reverse (lst)
             (fold-left lst 0 (lambda (acc next)
                                      (cons next acc))))
          nil
          nil) => {:result nil
                   :branches {}
                   :lambdas {
                             "reverse" [["LD 0 0" ["reverse"]]
                                        ["LDC 0"]
                                        ["LDF @$lambda-1"]
                                        ["LDF @fold-left"]
                                        ["AP 3"]
                                        ["RTN"]]
                             "$lambda-1" [["LD 0 1" ["$lambda-1"]]
                                          ["LD 0 0"]
                                          ["CONS"]
                                          ["RTN"]]}}
          (cleanup))

(fact "gcc reverse"
      (gcc '((defun fold-left (lst acc fun)
               (if (atom? lst)
                    acc
                    (fold-left (cdr lst)
                               (fun acc (car lst))
                               fun)))
             (defun reverse (lst)
               (fold-left lst 0 (lambda (acc next)
                                        (cons next acc))))))
      =>
      "LD 0 1 ; $lambda-1
LD 0 0
CONS
RTN
LD 0 0 ; reverse
LDC 0
LDF 0
LDF 10
AP 3
RTN
LD 0 0 ; fold-left
ATOM
TSEL 13 15
LD 0 1
RTN
LD 0 0
CDR
LD 0 1
LD 0 0
CAR
LD 0 2
AP 2
LD 0 2
LDF 10
TAP 3"
      (cleanup))

(fact "nth"
      (to-instruction-ast '(defun nth (lst i)
             (if (= i 0)
                  (car lst)
                  (nth (cdr lst)
                       (- i 1))))
          nil
          nil) => {:result nil
                   :branches {}
                   :lambdas {"nth" [["LD 0 1" ["nth"]]
                                    ["LDC 0"]
                                    ["CEQ"]
                                    ["TSEL @1 @4"]
                                    ["LD 0 0"]
                                    ["CAR"]
                                    ["RTN"]
                                    ["LD 0 0"]
                                    ["CDR"]
                                    ["LD 0 1"]
                                    ["LDC 1"]
                                    ["SUB"]
                                    ["LDF @nth"]
                                    ["TAP 2"]]}}
          (cleanup))

(fact "add-lines to nth"
      (let [past (to-instruction-ast '(defun nth (lst i)
                        (if (= i 0)
                             (car lst)
                             (nth (cdr lst)
                                  (- i 1))))
                     nil
                     nil)
            lams (:lambdas past)]
        (add-lines lams)) => [[0 "LD 0 1 ; nth"]
                              [1 "LDC 0"]
                              [2 "CEQ"]
                              [3 "TSEL 4 7"]
                              [4 "LD 0 0"]
                              [5 "CAR"]
                              [6 "RTN"]
                              [7 "LD 0 0"]
                              [8 "CDR"]
                              [9 "LD 0 1"]
                              [10 "LDC 1"]
                              [11 "SUB"]
                              [12 "LDF 0"]
                              [13 "TAP 2"]]
        (cleanup))

(fact "gcc nth"
      (gcc '((defun nth (lst i)
               (if (= i 0)
                    (car lst)
                    (nth (cdr lst)
                         (- i 1)))))) => "LD 0 1 ; nth
LDC 0
CEQ
TSEL 4 7
LD 0 0
CAR
RTN
LD 0 0
CDR
LD 0 1
LDC 1
SUB
LDF 0
TAP 2"
                        (cleanup))

(fact "fold-left"
      (to-instruction-ast '(defun fold-left (lst acc fun)
             (if (atom? lst)
                  acc
                  (fold-left (cdr lst)
                             (fun acc (car lst))
                             fun)))
          nil
          nil) => {:result nil
                   :branches {}
                   :lambdas {"fold-left"
                             [["LD 0 0" ["fold-left"]]
                              ["ATOM"]
                              ["TSEL @1 @3"]
                              ["LD 0 1"]
                              ["RTN"]
                              ["LD 0 0"]
                              ["CDR"]
                              ["LD 0 1"]
                              ["LD 0 0"]
                              ["CAR"]
                              ["LD 0 2"]
                              ["AP 2"]
                              ["LD 0 2"]
                              ["LDF @fold-left"]
                              ["TAP 3"]]}}
          (cleanup))


(fact "gcc fold-left"
      (gcc '((defun fold-left (lst acc fun)
               (if (atom? lst)
                    acc
                    (fold-left (cdr lst)
                               (fun acc (car lst))
                               fun)))))
      => "LD 0 0 ; fold-left
ATOM
TSEL 3 5
LD 0 1
RTN
LD 0 0
CDR
LD 0 1
LD 0 0
CAR
LD 0 2
AP 2
LD 0 2
LDF 0
TAP 3"
      (cleanup))

(fact "map"
      (to-instruction-ast '(defun map (lst fun)
             (reverse (fold-left lst
                        0
                        (lambda (acc next)
                                (cons (fun next) acc)))))
          nil
          nil)
      => {:result nil
          :branches {}
          :lambdas {"map" [["LD 0 0" ["map"]]
                           ["LDC 0"]
                           ["LDF @$lambda-1"]
                           ["LDF @fold-left"]
                           ["AP 3"]
                           ["LDF @reverse"]
                           ["AP 1"]
                           ["RTN"]]
                    "$lambda-1" [["LD 0 1" ["$lambda-1"]]
                                 ["LD 1 1"]
                                 ["AP 1"]
                                 ["LD 0 0"]
                                 ["CONS"]
                                 ["RTN"]]}}
      (cleanup))

(fact "gcc map"
      (gcc '((defun fold-left (lst acc fun)
               (if (atom? lst)
                    acc
                    (fold-left (cdr lst)
                               (fun acc (car lst))
                               fun)))
             (defun reverse (lst)
               (fold-left lst nil (lambda (acc next)
                                          (cons next acc))))
             (defun map (lst fun)
               (reverse (fold-left lst
                                   nil
                                   (lambda (acc next)
                                           (cons (fun next) acc)))))))
      => "LD 0 1 ; $lambda-2
LD 1 1
AP 1
LD 0 0
CONS
RTN
LD 0 0 ; map
LDC 0
LDF 0
LDF 24
AP 3
LDF 18
AP 1
RTN
LD 0 1 ; $lambda-1
LD 0 0
CONS
RTN
LD 0 0 ; reverse
LDC 0
LDF 14
LDF 24
AP 3
RTN
LD 0 0 ; fold-left
ATOM
TSEL 27 29
LD 0 1
RTN
LD 0 0
CDR
LD 0 1
LD 0 0
CAR
LD 0 2
AP 2
LD 0 2
LDF 24
TAP 3"
      (cleanup))

; (mk-list 5 23 nil) => '(23 23 23 23 23)
(fact "gcc mk-list"
      (gcc '((defun mk-list (size value lst)
               (if (= size 0)
                    lst
                    (mk-list (- size 1) value (cons value lst))))))
      => "LD 0 0 ; mk-list
LDC 0
CEQ
TSEL 4 6
LD 0 2
RTN
LD 0 0
LDC 1
SUB
LD 0 1
LD 0 1
LD 0 2
CONS
LDF 0
TAP 3"
      (cleanup))

(fact "gcc single value defun"
      (gcc '((defun twenty-three () 23)))
      => "LDC 23 ; twenty-three
RTN"
      (cleanup))

(fact "gcc range"
      (gcc '((defun range (n)
               (range-iter n nil))
             (defun range-iter (count out)
               (if (= count 0)
                    out
                    (range-iter (- count 1) (cons (- count 1) out))))))
      => "LD 0 0 ; range-iter
LDC 0
CEQ
TSEL 4 6
LD 0 1
RTN
LD 0 0
LDC 1
SUB
LD 0 0
LDC 1
SUB
LD 0 1
CONS
LDF 0
TAP 2
LD 0 0 ; range
LDC 0
LDF 0
AP 2
RTN"
      (cleanup))

(fact "gcc with main"
      (gcc '((defun x () 23)
             (defun main () 24)
             (defun y () 25)))
      =>
      "LDC 24 ; main
RTN
LDC 25 ; y
RTN
LDC 23 ; x
RTN"
      (cleanup))

(fact "add-lines with missing name"
      (add-lines {"some-fun" [["LDF @DNE"]]}) => (throws Exception))

(fact "mklists"
      (gcc '((defun x ()
               (mklist (mklist 1 2)))))
      => "LDC 1 ; x
LDC 2
LDC 0
CONS
CONS
LDC 0
CONS
RTN"
      (cleanup))

(fact "gcc ifs"
      (gcc '((defun blub ()
               (+ (if 0 1 2) (if 3 4 5)))))
      =>
      "LDC 0 ; blub
SEL 6 8
LDC 3
SEL 12 10
ADD
RTN
LDC 1 ; $blub-left-branch-1
JOIN
LDC 2 ; $blub-right-branch-2
JOIN
LDC 5 ; $blub-right-branch-4
JOIN
LDC 4 ; $blub-left-branch-3
JOIN"
      (cleanup))

(fact "let"
      (to-instruction-ast '(let ((x 1)
                 (y (- x 1)))
             (+ x y))
          {}
          {})
      =>
      {:result [["LDC 1"]
                ["LDF @$lambda-1"]
                ["AP 1"]]
       :lambdas {"$lambda-1"
                  [["LD 0 0" ["$lambda-1"]]
                   ["LDC 1"]
                   ["SUB"]
                   ["LDF @$lambda-2"]
                   ["AP 1"]
                   ["RTN"]]
                  "$lambda-2"
                  [["LD 1 0" ["$lambda-2"]]
                   ["LD 0 0"]
                   ["ADD"]
                   ["RTN"]]}
       :branches {}}
      (cleanup))
