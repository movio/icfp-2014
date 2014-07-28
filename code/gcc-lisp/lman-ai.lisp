;; lman ai

(
  ;; constants
  (defun PATH_LENGTH () 5)
  (defun SCORES ()
    (mklist
      0       ;; wall
      0       ;; empty
      10      ;; pill
      50      ;; power pill
      300     ;; fruit (if exists)
      -100    ;; ghost (normal)
      600     ;; ghost (fright)
      0))     ;; ghost (invisible)

  ;; entry point for each move

  (defun step (ai-state Q)
    (let ((lman-pos        (get-lman-pos Q))
          (urdl            (find-urdl lman-pos))
          (paths           ((get-paths-from Q) lman-pos))
          (score-at-pos    (get-score-at-pos Q))
          (scored-paths    (map paths
                                (lambda (path)
                                  (cons
                                    (fold-left path 0 (lambda (total next-pos)
                                                        (+ total (score-at-pos next-pos))))
                                    path))))
          (best-path       (fold-left scored-paths
                                      (cons -10000 0)
                                      (lambda (max next)
                                        (if (> (car next) (car max))
                                          next
                                          max))))
          (best-path-pos   (car (cdr best-path)))
          (best-path-dir   (first-index-of urdl (lambda (urdl-pos) (teq urdl-pos best-path-pos)))))
      ;;(dbg scored-paths)
      ;;(dbg best-path)
      ;;(dbg best-path-dir)
      (cons 0 best-path-dir)))

  ;; AI !

  ;; gets a function to score a position based on the current game state
  (defun get-score-at-pos (Q)
    (let ((at-pos          (close-1-1 at-world (get-world Q)))
          (ghost-poss      (map (get-ghost-states Q) get-ghost-pos))
          (is-ghost-at-pos (lambda (pos)
                             (exists ghost-poss (lambda (ghost-pos)
                                                  (teq pos ghost-pos)))))
          (fruit-state     (get-fruit-state Q)))
      (lambda (pos)
        (let ((world-value (at-pos pos)))                   ;; world value at position
          (if (is-ghost-at-pos pos)
            (nth (SCORES) 5)                                ;; if ghost return ghost score
                                                            ;; TODO: take vitality into account
            (if (or
                  (and (= world-value 4) (= 0 fruit-state)) ;; if fruit but not exist
                                                            ;; TODO: take ticks left into account
                  (>= world-value 5))                       ;; or starting locations
              (nth (SCORES) 1)                              ;; treat as empty
              (nth (SCORES) world-value)))))))              ;; else use value directly

  ;; returns all paths up to length PATH_LENGTH from the given position
  (defun get-paths-from (Q)
    (let ((at-pos             (close-1-1 at-world (get-world Q)))
          (is-not-wall-at-pos (lambda (pos) (> (at-pos pos) 0))))
      (lambda (pos)
        (get-paths-iter (mklist (mklist pos)) is-not-wall-at-pos (PATH_LENGTH)))))
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
