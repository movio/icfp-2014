;; lman ai

(
  ;; constants
  (defun PATH_LENGTH () 2)
  (defun SCORES ()
    (mklist
      0      ;; wall
      1      ;; empty
      2      ;; pill
      3      ;; poison
      4      ;; fruit (if exists)
      -1))   ;; ghost

  ;; entry point for each move

  (defun step (ai-state Q)
    (dbg (get-paths-from (lman-pos Q) (gen-is-not-wall-at Q))))

  ;; AI !

;; THIS FUNCTION IS BROKEN, WON'T COMPILE
;;  (defun at-world-scored (Q)
;;    (lambda (pos)
;;      (let ((ghost-poss (map (ghost-states Q) ghost-pos))
;;            (value      (if (exists ghost-poss (lambda (p) (teq pos p)))
;;                          5                            ;; override to ghost value if ghost
;;                          (if (= 0 (fruit-state Q))
;;                            1                          ;; override fruit to empty if not there
;;                            (at-world (world Q) pos))) ;; otherwise find value in world
;;            (score (nth (SCORES) val)))
;;        score))))

  (defun gen-is-not-wall-at (Q)
    (let ((this-world (world Q))
          (gen-fun    (close-1-1 at-world this-world)))
      (lambda (pos)
        (> (gen-fun pos) 0))))

  ;; helper functions

  ; returns [path]
  (defun get-paths-from (pos is-not-wall)
    (get-paths-iter (mklist (mklist pos)) is-not-wall (PATH_LENGTH)))
  (defun get-paths-iter (paths is-not-wall depth)
    (if (= 0 depth)
      paths
      (get-paths-iter
        (mapcat paths (lambda (path)
                        (map
                          (filter
                            (find-urdl (car path))               ;; positions to consider
                            (lambda (pos)
                              (and
                                (not (exists path
                                       (lambda (old-pos)
                                         (teq old-pos pos))))    ;; exclude spaces i've already been on
                                (is-not-wall pos))))             ;; excluding walls
                          (lambda (pos)
                            (cons pos path)))))                  ;; prepended to the existing path
        is-not-wall
        (- depth 1))))

  (defun find-urdl (pos)
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

  (defun fruit-state (Q)
    (nth Q 3))

  (defun ghost-pos (ghost-state)
    (nth ghost-state 1))

  (defun ghost-states (Q)
    (nth Q 2))

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
                (find-urdl (lman-pos Q))
                (close-1-1 at-world (world Q)))
              (lambda (x)
                (if (>= x 3) 1 x)))) ;; normalise world
          (lambda (v-dir)
            (not (= (cdr v-dir) (nth (mklist 2 3 0 1) (lman-dir Q)))))) ;; lookup table for opposite dir
        (cons 0 0)
        (lambda (max candidate)
          (if (>= (car candidate) (car max))
            candidate
            max)))))
)
