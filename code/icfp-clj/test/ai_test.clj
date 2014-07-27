(ns ai-test
  (:use clojure.test)
  (:use ai)
  )

" Build World 
* 0: Wall (`#`)
* 1: Empty (`<space>`)
* 2: Pill 
* 3: Power pill
* 4: Fruit location
* 5: Lambda-Man starting position
* 6: Ghost starting position
Expected: 
((0,1) (0,2) (0,3) (0,4))
((0,1) (0,2) (0,3) (1,3))
((1,0) (2,0) (3,0) (4,0))
((1,0) (2,0) (2,1) (2,2))
"
(def world1
  (list 
    "0000000"
    "0122220"
    "0200200" 
    "0222000"
    "0200000"
    "0200000"
    "0000000"
    ))

(deftest getOptionsTest (is (= (worldGet '(1 2) world1) 5)))


(deftest getContentsTest
  (is (= (getContents '(-1 0) world1) 0))
  (is (= (getContents '(0 -1) world1) 0))
  (is (= (getContents '(1 1)  world1) 1))
  (is (= (getContents '(2 1)  world1) 2))
  )


(deftest getOptionsTest
  (is (= (getOptions '(1 1) '(1 1) world1) (list (list 2 '(2 1)) (list 2 '(1 2))) ))
  (is (= (getOptions '(1 2) '(1 1) world1) (list (list 2 '(1 3))) ))
  )


(deftest getPathsTest
  (def world1
    (list 
      "0000000"
      "0122220"
      "0200200" 
      "0222000"
      "0200000"
      "0200000"
      "0000000"
      ))
  (is (= (orderPaths (getPaths (list (list (list 1 1))) 0 4 world1))
         (list
           (list (list 2 1) (list 3 1) (list 4 1) (list 5 1))
           (list (list 2 1) (list 3 1) (list 4 1) (list 4 2))
           (list (list 1 2) (list 1 3) (list 2 3) (list 3 3))
           (list (list 1 2) (list 1 3) (list 1 4) (list 1 5))
           ))

      )
  )


(deftest pathScoreTest 
  (def world
    (list 
      "0000000"
      "0122320"))
  (is (= (pathScore (list (list 2 1) (list 3 1) (list 4 1) (list 5 1)) 0 world) 9))
  )


(deftest getBestPathTest
  (def world
    (list 
      "0000000"
      "0122290"
      "0200200" 
      "0222000"
      "0200000"
      "0200000"
      "0000000"
      ))
  (is (= (getBestPath '(1 1) 4 world) (list 15 '(2 1))))
  )
