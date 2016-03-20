(ns proto-repl-visualizations-demo.self-hosted-visual-ext
  "This example should be run in the Self Hosted REPL. It shows the major
   extension points for creating your own visualizations. After opening up the
   self hosted REPL evaluate each form separately."
  (:require [clojure.string :as str]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A super basic example of regisetering a code execution


(defn handle-display-callback
  [data]
  (println "handle-display-callback:" (pr-str (js->clj data))))

;; Register the code execution extension
(js/protoRepl.registerCodeExecutionExtension
 ;; The name of the code execution extension
 "self-hosted-println-demo"
 ;; A callback function to invoke. Using an anonymous function here so we can
 ;; redefine handle-display-callback to change the behavior
 #(handle-display-callback %))


;; The code execution is triggered when Proto REPL evaluates some code that
;; returns something like this.
[:proto-repl-code-execution-extension
 "self-hosted-println-demo"
 {:some-data ["for" "display"]}]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; A more involved example that creates a new view and displays data to it.

;; Define a View in Atom
(deftype ElementView [title element]
  Object
  (getTitle [this] title)
  (getElement [this] element))

(defn element-view
  "Creates a new element view"
  [title]
  (ElementView. title (js/document.createElement "div")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Create and register a URI handler in Atom.
(defn uri-opener-handler
  "Handler for when a URI is opened in Atom. If the ur has the right protocol
   then an element view is created and returned."
  [uri-to-open]
  (println "uri-opener-handler" uri-to-open)
  (try
    (when-let [[_ title] (re-find #"self-hosted-view://(.*)" uri-to-open)]
      (element-view (js/decodeURIComponent title)))
    (catch js/Error error
      (println error)
      (throw error))))

;; Register a URI opener in atom
(js/atom.workspace.addOpener #(uri-opener-handler %))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Define functions for opening a new view using the URI mechanism.
(defn div-with-content
  "Creates a div with the given text content"
  [content]
  (let [div (js/document.createElement "div")]
    (aset div "textContent" content)
    div))

(defn open-view-with-content
  "Opens a new view with the given content."
  [title content]
  (let [uri (str "self-hosted-view://" title)
        view-promise (js/atom.workspace.open uri (clj->js {:split "right"}))]
    (.done
     view-promise
     (fn [view]
       (try
        (let [hello-div (div-with-content content)]
          (.appendChild (.getElement view) hello-div))
        (catch js/Error error
          (println error)
          (throw error)))))))

(open-view-with-content "Test title" "Hi there!")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Create a code execution extension that will open the view
(defn open-view-with-content-handler
  [data]
  (let [{:strs [title content]} (js->clj data)]
    (open-view-with-content title content)))


;; Register the code execution extension
(js/protoRepl.registerCodeExecutionExtension
 ;; The name of the code execution extension
 "self-hosted-view-demo"
 ;; A callback function to invoke. Using an anonymous function here so we can
 ;; redefine open-view-with-content-handler to change the behavior
 #(open-view-with-content-handler %))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; This function would go on the Clojure side for displaying the data in Atom.
;; It just returns the special key and data. Proto REPL recognizes the special
;; key and finds a registered code execution extension and invokes that.
(defn show-content
  [title content]
  [:proto-repl-code-execution-extension
   "self-hosted-view-demo"
   {:title title :content content}])


;; Using the helper function
(show-content "My data that I want to display" (pr-str (range 27)))
