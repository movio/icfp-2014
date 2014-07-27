;; lman ai

(
  ;; constants
  (defun PATH_LENGTH () 1)

  ;; entry point for each move

  (defun step (ai-state Q)
    (cons 0 (old-ai Q)))

  ;; helper functions

  ; returns [path]
  (defun get-paths-from (pos)
    (get-paths-iter (mklist (mklist pos)) (PATH_LENGTH)))
  (defun get-paths-iter (paths depth)
    (if (= 0 depth)
      paths
      (get-paths-iter
        (mapcat paths (lambda (path)
                        (map
                          (urdl (car path))                 ;; positions to consider
                          (lambda (pos)
                            (if (get-paths-filter pos path) ;; remove walls and last-pos
                              nil                           ;; gets removed by the concat (mapcat)
                              (cons pos path))))))
        (- depth 1))))
  (defun get-paths-filter (pos path)
    (teq pos (get-paths-last-pos path)))
  (defun get-paths-last-pos (path)
    (if (= 1 (length path))
      (car path)
      (nth path 1))) ;; zero-indexed

  (defun urdl (pos)
    ((lambda (x y)
      (mklist
        (cons x (- y 1))
        (cons (+ x 1) y)
        (cons x (+ y 1))
        (cons (- x 1) y)))
     (car pos)
     (cdr pos)))

  (defun at-world (world pos)
    (nth (nth world (cdr pos)) (car pos)))

  ;; extractors from game-state

  (defun lman-pos (Q)
    (nth (lman-state Q) 1))

  (defun lman-dir (Q)
    (nth (lman-state Q) 2))

  (defun lman-state (Q)
    (nth Q 1))

  (defun world (Q)
    (nth Q 0))

  ;; OLD AI

  (defun old-ai (Q)
    (cdr
      (fold-left
        (filter
          (zip-with-index
            (map
              (map
                (urdl (lman-pos Q))
                (close-1-1 at-world (world Q)))
              (lambda (x)
                (if (>= x 3) 1 x))))
          (lambda (v-dir)
            (not (= (cdr v-dir) (nth (mklist 2 3 0 1) (lman-dir Q))))))
        (cons 0 0)
        (lambda (max candidate)
          (if (>= (car candidate) (car max))
            candidate
            max)))))
)
