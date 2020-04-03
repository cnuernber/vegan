(defproject vegan "0.1-SNAPSHOT"
  :description "Render vega to png and svg using node"
  :url "http://github.com/cnuernber/vegan"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"]]
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.19"]]

  :profiles {:dev {:dependencies [[cider/piggieback "0.4.2"]
                                  [figwheel-sidecar "0.5.19"]]
                   :repl-options {:nrepl-middleware
                                  [cider.piggieback/wrap-cljs-repl]}}}

  :figwheel {:readline false}
  :cljsbuild {:builds [{:id "app"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main vegan.app
                                   :asset-path "node/out"
                                   :target :nodejs
                                   :externs ["externs.js"]
                                   :output-to "node/app.js"
                                   :output-dir "node/out"
                                   :optimizations :none
                                   :source-map true}}

                       {:id "prod"
                        :source-paths ["src"]
                        :compiler {:main vegan.app
                                   :asset-path "prod/out"
                                   :target :nodejs
                                   :externs ["externs.js"]
                                   :output-to "prod/app.js"
                                   :output-dir "prod/out"
                                   :optimizations :advanced}}]})
