(defproject eventorg "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GNU GPL v3"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.2"]
                 [compojure "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [http-kit "2.1.18"]
                 [revise "0.0.6"]
                 [com.cemerick/friend "0.2.1"]
                 [rethinkdb "0.4.39"]
                 [clj-http "1.1.1"]]
  :main ^:skip-aot eventorg.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
