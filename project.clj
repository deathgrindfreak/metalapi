(defproject metallumapi "0.0.1"
  :description "An api for traversing Encyclopedia Metallum."
  :url "http://metalapi.com/"
  :license {:name "BSD 3 Clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2069"]
                 [enlive "1.1.5"]
                 [ring "1.4.0-RC1"]]
  :plugins [[lein-cljsbuild "1.0.0"]]
  :cljsbuild {:builds
              [{:source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/main.js"
                           :optimizations :whitespace
                           :pretty-print true}}]})
