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
      (mklist 0 0 0 0 0)
      (mklist 0 0 2 0 0)
      (mklist 0 4 5 6 0)
      (mklist 0 0 0 0 0)))

  (defun test-lman-state ()
    (mktuple
      0          ;; vitality
      (cons 2 2) ;; position
      2          ;; direction
      2          ;; remaining lives
      100))      ;; score

  (defun test-ghosts-state ()
    (mktuple
      (mktuple
        0          ;; vitality
        (cons 3 2) ;; position
        3)))       ;; direction

  (defun test-fruit ()
    100) ;; ticks remaining

  (defun test-game-state ()
    (mktuple
      (test-world)
      (test-lman-state)
      (test-ghosts-state)
      (test-fruit)))

  (defun main (game-state ghost-prgs)
    (step 0 (test-game-state)))
)
