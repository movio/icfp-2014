;; general utility functions

(
  (defun close-1-1 (fun var1)
    (lambda (var2) (fun var1 var2)))

  (defun close-4-1 (fun var1 var2 var3 var4)
    (lambda (var5) (fun var1 var2 var3 var4 var5)))

  (defun not (x)
    (= 0 x))

  (defun and (x y)
    (* (= 0 (= 0 x)) (= 0 (= 0 y))))       ;; double inline not for normalisation

  (defun or (x y)
    (> (+ (= 0 (= 0 x)) (= 0 (= 0 y))) 0)) ;; double inline not for normalisation

  (defun mod (n m)
    (if (>= n m)
      (mod (- n m) m)
      (if (>= n 0)
        n
        (mod (+ n m) m))))

  (defun pow (b e)
    (pow-iter b e 1))
  (defun pow-iter (b times-left out)
    (if (>= 0 times-left)
      out
      (pow-iter b (- times-left 1) (* out b))))
)
