(defproject test-project "1.0-SNAPSHOT"
  :description "A test project."
  :url "http://my-cool-project.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 ]
  :dev-dependencies [[leiningen/lein-swank "1.4.5"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.5.0"]]}}
)
