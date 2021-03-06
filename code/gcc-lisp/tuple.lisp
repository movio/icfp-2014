;; tuple functions

(
  (defun nth (tpl n)
    (if (>= 0 n)
      (if (atom? tpl)
        tpl
        (car tpl))
      (nth (cdr tpl) (- n 1))))

  (defun teq (a b)
    (if (= (length a) (length b)) ;; optimisation: will be 1 less than actual length
      (teq-iter a b)
      0))
  (defun teq-iter (a b)
    (if (atom? a) ;; if called through teq then implies (atom b)
      (= a b)
      (if (= (car a) (car b))
        (teq-iter (cdr a) (cdr b))
        0)))

  (defun tlength (a)
    (+ 1 (length a)))
)
