(defproject proto-repl-visualizations-demo "0.1.0-SNAPSHOT"
  :description "A demonstration project for Proto REPL and "
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [proto-repl "0.1.2"]
                 [proto-repl-charts "0.2.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/java.classpath "0.2.3"]
                 [clj-time "0.11.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [clj-jgit "0.8.8"]]

  :profiles
  {:dev {:source-paths ["dev" "src" "test"]
         :dependencies []}})
