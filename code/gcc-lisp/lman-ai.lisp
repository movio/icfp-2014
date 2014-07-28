;; lman ai

(
  ;; constants
  (defun BAD_PATH () -2000000000)
  (defun PATH_LENGTH () 6)
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
          (lman-dir        (get-lman-dir Q))
          (ghost1-dir      (get-ghost-dir (car (get-ghost-states Q))))
          (urdl            (find-urdl lman-pos))
          (paths           ((get-paths-from Q) lman-pos))
          (score-at-pos    (get-score-at-pos Q))
          (scored-paths    (map paths
                                (lambda (path)
                                  (cons
                                    (fold-left (zip-with-index path) 0
                                               (lambda (total pos-dist)
                                                 (let ((next-pos   (car pos-dist))
                                                       (dist       (cdr pos-dist))
                                                       (raw-score  (score-at-pos next-pos))
                                                       (dist-score (if (= 0 raw-score)
                                                                     0
                                                                     (/ (* 10000 raw-score) (pow 2 dist)))))
                                                   (+ total dist-score))))
                                    path))))
          (urdl-score-sum  (map urdl
                                (lambda (dir-pos)
                                  (fold-left scored-paths (cons 0 0)
                                             (lambda (total scored-path)
                                               (let ((score (car total))
                                                     (count (cdr total)))
                                                 (if (teq dir-pos (car (cdr scored-path))) ;; if this path starts in this dir
                                                   (cons (+ score (car scored-path)) (+ count 1))
                                                   total)))))))
          (urdl-score-avg  (map urdl-score-sum
                                 (lambda (sum-count)
                                   (let ((sum   (car sum-count))
                                         (count (cdr sum-count)))
                                     (if (= 0 count)
                                       (BAD_PATH)                  ;; mark empty directions with sentinel value
                                       (/ sum count))))))
          (best-score-dirs (fold-left (zip-with-index urdl-score-avg) (mklist (cons (BAD_PATH) -1))
                                      (lambda (max score-dir)
                                        (let ((score     (car score-dir))
                                              (max-score (car (car max)))) ;; car of head of list
                                          (if (= score max-score)
                                            (cons score-dir max)
                                            (if (> score max-score)
                                              (mklist score-dir)
                                              max))))))
          (best-dirs       (map best-score-dirs (lambda (score-dir)
                                                  (cdr score-dir))))
          (chosen-dir      (let ((dirs (map (range 4)
                                            (lambda (x) (mod (+ x ghost1-dir) 4))))) ;; enum dirs from cur ghost1 dir cw
                             (fold-left dirs -1 (lambda (chosen-dir next-dir)
                                                  (if (> chosen-dir -1)
                                                    chosen-dir
                                                    (if (exists best-dirs (lambda (dir) (= next-dir dir)))
                                                      next-dir
                                                      -1))))))
          )
      ;;(dbg scored-paths)
      ;;(dbg urdl-score-avg)
      ;;(dbg best-score-dirs)
      ;;(dbg chosen-dir)
      (cons 0 chosen-dir)
      ))

  ;; gets a function to score a position based on the current game state
  (defun get-score-at-pos (Q)
    (let ((at-pos          (close-1-1 at-world (get-world Q)))
          (ghost-states    (get-ghost-states Q))
          (ghost-at-pos    (lambda (pos)
                             (fold-left (zip-with-index ghost-states) -1
                                        (lambda (found next-ghost-state)
                                          (if (> found -1)
                                            found
                                            (let ((ghost-state (car next-ghost-state))
                                                  (index       (cdr next-ghost-state)))
                                              (if (teq pos (get-ghost-pos ghost-state))
                                                index
                                                -1)))))))
          (fruit-state     (get-fruit-state Q)))
      (lambda (pos)
        (let ((world-value (at-pos pos))                    ;; world value at position
              (ghost-index (ghost-at-pos pos)))             ;; ghost at position else -1
          (if (> ghost-index -1)
            (let ((ghost-vit (get-ghost-vit (nth ghost-states ghost-index))))
              (nth (SCORES) (+ 5 ghost-vit)))               ;; if ghost return ghost score based on vit
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

  (defun get-ghost-dir (ghost-state)
    (nth ghost-state 2))

  (defun get-ghost-pos (ghost-state)
    (nth ghost-state 1))

  (defun get-ghost-vit (ghost-state)
    (nth ghost-state 0))

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
