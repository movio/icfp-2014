;; general utility functions

(
  (defun close-1-1 (fun var1)
    (lambda (var2) (fun var1 var2)))

  (defun not (x)
    (= x 0))
)
