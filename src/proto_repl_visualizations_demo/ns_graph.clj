(ns proto-repl-visualizations-demo.ns-graph
  "Creates a graph of namespaces. Based on https://github.com/hilverd/lein-ns-dep-graph
   distributed under EPL license."
  (:require [clojure.java.io :as io]
            [clojure.java.classpath :as cp]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            prc
            [clojure.tools.namespace.file :as ns-file]
            [clojure.tools.namespace.track :as ns-track]
            [clojure.tools.namespace.parse :as ns-parse]
            [clojure.tools.namespace.find :as ns-find]
            [clojure.tools.namespace.dependency :as ns-dep]))

(defn- jar-file->dep-map [jar-file]
  "Takes a JarFile and returns a dependency map"
  (reduce (fn [m file]
            (if-let [decl (ns-find/read-ns-decl-from-jarfile-entry jar-file file)]
              (let [deps (ns-parse/deps-from-ns-decl decl)
                    name (second decl)]
                (assoc m name deps))
              m))
          {} (ns-find/clojure-sources-in-jar jar-file)))

(defn applicable-ns?
 "Takes a namespace symbol and returns true if it's applicable for display."
 [node]
 (let [node-name (name node)]
   (and (not (str/starts-with? node-name "compliment"))
        (not (str/starts-with? node-name "defprecated"))
        (not (str/starts-with? node-name "complete.core"))
        (if (str/starts-with? node-name "clojure")
          (re-find #"\.tools\.namespace" node-name)
          true))))

(defn applicable-edge?
  "Takes an edge of two namespace symbols and returns true if it's applicable."
  [[n1 n2]]
  (and (applicable-ns? n1) (applicable-ns? n2)))

(def ns-graph
  "The clojure.tools.namespace graph of namespace dependencies."
  (let [project-tracker (ns-file/add-files
                         {}
                         (ns-find/find-clojure-sources-in-dir (io/file "src")))]
    (::ns-track/deps
     (reduce ns-track/add
             project-tracker
             (map jar-file->dep-map (cp/classpath-jarfiles))))))


(defn ns->node
  "Takes a namespace and returns a node map. Group is used to identify unique sets of namespaces."
  [ns-sym]
  (let [ns-name (name ns-sym)]
    {:id ns-sym
     :label ns-sym
     :group (second (re-find #"(.+)\.[^.]+" ns-name))}))

(def nodes
  "The nodes to display for the graph"
  (->> (ns-dep/nodes ns-graph)
       (filter applicable-ns?)
       (map ns->node)))

(def edges
  "The edges to display for the graph"
  (->> nodes
       (map :id)
       (mapcat
        (fn [node]
          (for [dep (ns-dep/immediate-dependencies ns-graph node)]
            [node dep])))
       (filter applicable-edge?)))

(defn read-node-name-handler
  [click-data]
  (let [node (-> click-data :nodes first :label)]
    (sh/sh "say" node)))

(def excluded-nss (atom #{}))

(defn exclude-node-handler
  [click-data]
  (let [exclude-ns (-> click-data :nodes first :label symbol)
        _ (swap! excluded-nss conj exclude-ns)
        filtered-nodes (filter #(not (contains? @excluded-nss (:id %))) nodes)
        filtered-edges (filter #(not (or (contains? @excluded-nss (first %))
                                         (contains? @excluded-nss (second %))))
                                edges)]
    (prc/graph
     "ns-graph"
     {:nodes filtered-nodes
      :edges filtered-edges}
     {:edges {:arrows "to"}
      :events {:doubleClick 'proto-repl-visualizations-demo.ns-graph/handle-double-click}})))

(defn handle-double-click
  [click-data]
  (read-node-name-handler click-data))
  ; (exclude-node-handler click-data))

(defn draw-full-graph
  []
  (prc/graph
   "ns-graph"
   ;; The graph
   {:nodes nodes
    :edges edges}
   ;; Options
   {:edges {:arrows "to"}
    :events {:doubleClick 'proto-repl-visualizations-demo.ns-graph/handle-double-click}
    :physics {:stabilization false
               :barnesHut {:gravitationalConstant -2000
                           :springConstant 0.01}}}))


(draw-full-graph)
