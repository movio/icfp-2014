(def main
  (lambda (game undefined)
    (cons 0 (gen-step game))))

(def gen-step
  (lambda (game)
    (test game)
    (lambda () (cons 0 1))))

(def nth
  (lambda (lst i)
    (if (= 0 i)
      (head lst)
      (nth (tail lst) (- i 1)))))

(def xy
  (lambda (world x y)
    (nth (nth world y) x)))

(def test
  (lambda (game)
    (def world (nth game 0))
    (def res (xy world 3 3))
    (dbug res)
    res))

(main)
