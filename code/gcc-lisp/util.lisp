;; general utility functions

(
  (defun close-1-1 (fun var1)
    (lambda (var2) (fun var1 var2)))

  (defun not (x)
    (= 0 x))

  (defun and (x y)
    (* (= 0 (= 0 x)) (= 0 (= 0 y))))       ;; double inline not for normalisation

  (defun or (x y)
    (> (+ (= 0 (= 0 x)) (= 0 (= 0 y))) 0)) ;; double inline not for normalisation
)
