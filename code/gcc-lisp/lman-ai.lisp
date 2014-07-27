;; lman ai

(
  (defun at-world (world pos)
    (nth (nth world (cdr pos)) (car pos)))

  (defun extract-world (game-state)
    (nth game-state 0))

  (defun urdl (pos)
    ((lambda (x y)
      (mktuple
        (cons x (- y 1))
        (cons (+ x 1) y)
        (cons x (+ y 1))
        (cons (- x 1) y)))
     (car pos)
     (cdr pos)))

  (defun step (ai-state game-state)
    (dbg (extract-world game-state)))
)
