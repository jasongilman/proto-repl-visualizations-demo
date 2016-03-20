(ns proto-repl-visualizations-demo.diagramming)

(defn service
  "Defines a service with the given name. It will be displayed with a box."
  ([name]
   (service "services" name))
  ([group name]
   {:id name
    :label name
    :shape :box
    :group group}))

(defn database
  "Defines a database with the given name. It will be displayed with a cylinder"
  [name]
  {:id name
   :label name
   :shape :database
   :size 20
   :group "databases"})

(defn user
  "Defines a user with the given name. It will be displayed with a profile icon"
  [name]
  {:id name
   :label name
   :shape :icon
   :icon {:face "Ionicons"
          :code "\uf47e"
          :size 50
          :color "#aa00ff"}})

(defn edge
  "Defines edges between two components with an optional label."
  ([comp1 comp2]
   (edge comp1 comp2 nil))
  ([comp1 comp2 label]
   (if label
     {:from comp1 :to comp2 :label label}
     {:from comp1 :to comp2})))

(def graph-options
  "Visjs Options for displaying the graph. See http://visjs.org/docs/network/"
  {
   :layout {:hierarchical {:sortMethod :directed
                           :nodeSpacing 300}}
   :edges {:smooth {:type :straightCross}
           :arrows {:to true}
           :arrowStrikethrough false
           :font {:align :horizontal}}
   :physics {:enabled false}})


(prc/graph
 "My Fictitious System"
 {:nodes (concat (map service [:authentication :shopping-cart :order-processing])
                 (map database [:metrics :users :orders :inventory])
                 (map user [:shopper :admin]))
  :edges (map (partial apply edge)
              [[:authentication :users]
               [:shopper :shopping-cart "Shopping"]
               [:shopper :authentication "Logging In"]
               [:shopping-cart :authentication]
               [:shopping-cart :order-processing]
               [:shopping-cart :inventory]
               [:order-processing :orders]
               [:order-processing :inventory]
               [:user :shopping-cart]
               [:order-processing :metrics]
               [:admin :metrics "View Metrics"]])}
 graph-options)
