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
    (let ((world          (get-world Q))
          (lman-pos       (get-lman-pos Q))
          (at-pos         (close-1-1 at-world world)))
      (dbg (get-paths-from lman-pos (lambda (pos) (> (at-pos pos) 0))))))

  ;; AI !

;; THIS FUNCTION IS BROKEN, WON'T COMPILE - probably mismatched parens
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

  ;; returns all paths up to length PATH_LENGTH from the given position
  (defun get-paths-from (pos is-not-wall)
    (get-paths-iter (mklist (mklist pos)) is-not-wall (PATH_LENGTH)))
  (defun get-paths-iter (paths is-not-wall depth)
    (if (= 0 depth)
      (map paths (lambda (path)           ;; if done
                   (cdr (reverse path)))) ;; drop current position and correct order in list
      (get-paths-iter                     ;; otherwise recursively extend each path
        (mapcat paths (lambda (path)
                        (let ((good-urdl (filter
                                          (find-urdl (car path))            ;; positions to consider
                                          (lambda (pos)
                                            (and
                                              (not (exists path
                                                     (lambda (old-pos)
                                                       (teq old-pos pos)))) ;; exclude visited spaces
                                              (is-not-wall pos))))))        ;; exclude walls
                          (if (atom? good-urdl)       ;; if no more good positions
                            (mklist path)             ;; return path so we don't lose it
                            (map
                              good-urdl
                              (lambda (pos)
                                (cons pos path))))))) ;; otherwise prepend each spot to the existing path
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

  (defun get-fruit-state (Q)
    (nth Q 3))

  (defun get-ghost-pos (ghost-state)
    (nth ghost-state 1))

  (defun get-ghost-states (Q)
    (nth Q 2))

  (defun get-lman-pos (Q)
    (nth (get-lman-state Q) 1))

  (defun get-lman-dir (Q)
    (nth (get-lman-state Q) 2))

  (defun get-lman-state (Q)
    (nth Q 1))

  (defun get-world (Q)
    (nth Q 0))

  ;; OLD AI

  (defun old-ai (Q)
    (cdr
      (fold-left
        (filter
          (zip-with-index
            (map
              (map
                (find-urdl (get-lman-pos Q))
                (close-1-1 at-world (get-world Q)))
              (lambda (x)
                (if (>= x 3) 1 x)))) ;; normalise world
          (lambda (v-dir)
            (not (= (cdr v-dir) (nth (mklist 2 3 0 1) (get-lman-dir Q)))))) ;; lookup table for opposite dir
        (cons 0 0)
        (lambda (max candidate)
          (if (> (car candidate) (car max))
            candidate
            max)))))
)
