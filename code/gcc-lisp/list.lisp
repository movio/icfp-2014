;; list utility functions

(
  (defun leq (a b)
    (if (= (length a) (length b))
      (fold-left (zip a b) 1 (lambda (acc next)
                                 (if (= 0 acc)
                                   0
                                   (= (car next) (cdr next)))))
      0))

  (defun fold-left (lst acc fun)
    (if (atom? lst)
      acc
      (fold-left (cdr lst) (fun acc (car lst)) fun)))

  (defun reverse (lst)
    (fold-left lst nil (lambda (acc next)
                         (cons next acc))))

  (defun map (lst fun)
    (reverse
      (fold-left lst nil (lambda (acc next)
                           (cons (fun next) acc)))))

  (defun filter (lst pred)
    (reverse
      (fold-left lst nil (lambda (acc next)
                           (if (pred next)
                             (cons next acc)
                             acc)))))

  (defun concat (lst)
    (reverse
      (fold-left lst nil (lambda (acc next)
                           (if (atom? next)
                             (cons next acc)
                             (fold-left next acc (lambda (acc next)
                                                   (cons next acc))))))))

  (defun mapcat (lst fun)
    (concat (map lst fun)))

  (defun length (lst)
    (fold-left lst 0 (lambda (acc next)
                       (+ acc 1))))

  (defun range (n)
    (range-iter n nil))
  (defun range-iter (count out)
    (if (= count 0)
      out
      ((lambda (n) (range-iter n (cons n out))) (- count 1))))

  (defun zip (a b)
    (zip-iter a b nil))
  (defun zip-iter (a b out)
    (if (atom? a)
      out
      (if (atom? b)
        out
        (zip-iter
          (cdr a)
          (cdr b)
          (cons (cons (car a) (car b)) out)))))
)
