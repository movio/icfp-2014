(ns ai)


(defn worldGet [pos world] (Integer. (str (nth (nth world (second pos)) (first pos)))))


(defn getContents [pos world]
  (if (or (< (first pos) 0) (< (second pos) 0))
    0
    (Integer. (str (worldGet pos world)))))


" Returns: (list (list scrore (list x y))"
(defn getOptions [pos lastPos world]
  (def x (first pos))
  (def y (second pos))
  (def lines
    (list 
      (list (getContents (list (- x 1) y) world) (list (- x 1) y)) ; up
      (list (getContents (list x (- y 1)) world) (list x (- y 1))) ; left
      (list (getContents (list (+ x 1) y) world) (list (+ x 1) y)) ; down 
      (list (getContents (list x (+ y 1)) world) (list x (+ y 1))) ; right
      ))
  (def noWalls 
    (filter 
      (fn [line]
        (not= (first line) 0))
      lines))
  (filter 
    (fn [line]
      (not= (second line) lastPos))
    noWalls)
  )


;(list (list (list x y))))
(defn getPaths [paths currentStep maxStep world]
  (if (= currentStep maxStep)
    paths
    (do 
      ; (list (list scrore paths ))
      (def newPaths  
        (mapcat
          (fn [path] ; (list paths)
            ; Set next to first if second doesn't exist
            (def lastPos (if (< (count path) 2) (first path) (second path)))
            (def currentPos (first path))
            (def options (getOptions currentPos lastPos world))
            (map 
              (fn [option]
                (cons (second option) path)
                )
              options)
            )
          paths
          )
        )
      (getPaths newPaths (+ currentStep 1) maxStep world)
      )))


(defn orderPaths [paths]
  (map (fn [path]
         (rest (reverse path))
         )
       paths ))

(defn pathScore [path score world]
  (if (empty? path) score
    (pathScore (rest path) (+ score (worldGet (first path) world)) world)
    )
  )


(defn getBestScore [scores bestScore coord]
  (if (empty? scores)
    (list bestScore coord)
    (do
      (def score (first scores))
      (if (> (first score) bestScore)
        (getBestScore (rest scores) (first score) (second score))
        (getBestScore (rest scores) bestScore coord)
        )
      )
    )
  )


(defn getBestPath [currentPos steps world]
  (def allPaths (orderPaths (getPaths (list (list currentPos)) 0 steps world)))
  (def scores 
    (map 
      ; for now just the best not the best direction
      (fn [path]
        (list 
          (pathScore path 0 world)
          (first path))
        )
      allPaths)
    )
  (getBestScore scores 0 currentPos)
  )

