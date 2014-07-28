;; for running on the debugger

(
  ;; 0 = wall
  ;; 1 = empty
  ;; 2 = pill
  ;; 3 = power pill
  ;; 4 = fruit location
  ;; 5 = pacman start
  ;; 6 = ghost start
  (defun test-world ()
    (mklist
      (mklist 0 0 0 0 0 0 0)
      (mklist 0 5 2 2 2 2 0)
      (mklist 0 2 0 0 2 0 0)
      (mklist 0 2 2 2 0 0 0)
      (mklist 0 2 0 0 0 1 0)
      (mklist 0 2 0 0 1 6 0)
      (mklist 0 0 0 0 0 0 0)))

  (defun test-lman-state ()
    (mktuple
      0          ;; vitality
      (cons 1 1) ;; position
      2          ;; direction
      2          ;; remaining lives
      100))      ;; score

  (defun test-ghosts-state ()
    (mklist
      (mktuple
        0          ;; vitality
        (cons 5 5) ;; position
        3)))       ;; direction

  (defun test-fruit ()
    0) ;; ticks remaining

  (defun test-game-state ()
    (mktuple
      (test-world)
      (test-lman-state)
      (test-ghosts-state)
      (test-fruit)))

  (defun main (game-state ghost-prgs)
    (step 0 (test-game-state)))
)
