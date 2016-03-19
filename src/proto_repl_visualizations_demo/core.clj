(ns proto-repl-visualizations-demo.core
  (:require [clojure.data.csv :as csv]
            [clojure.string :as str]
            [clj-time.core :as t]
            prc
            [clj-time.format :as fmt]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitq]
            [clj-time.coerce :as c]))

(def row-date-format
  (fmt/formatter "M/d"))

(defn parse-row
  [[date-str downloads-str]]
  {:date (fmt/parse row-date-format date-str)
   :downloads (Long. downloads-str)})

(defn get-downloads
  []
  (->> (slurp "proto_repl_downloads.csv")
       (csv/read-csv)
       (map parse-row)))

(def downloads
  (get-downloads))

(def downloads-per-date
  (map #(update %2 :downloads - (:downloads %1))
       downloads
       (drop 1 downloads)))

(def chart-date-format
  (fmt/formatter "yyyy-MM-dd"))

(prc/custom-chart
 "Downloads Per Version"
 {:data {:x "x"
         :columns
         [(cons "x" (map #(fmt/unparse chart-date-format (:date %)) downloads-per-date))
          (cons "downloads" (map :downloads downloads-per-date))]
         :type "bar"}
  :axis {:x {:type "timeseries"
             :tick {:format "%m-%d"}}}})


(comment
 ;; Code for parsing and potentially displaying git releases

 (defn parse-git-commit
   [commit]
   (let [[_ version] (re-find #"Prepare ([^\s]+) release" (:message commit))
         time (c/from-date (:time commit))]
     {:time time
      :version version}))

 (defn get-version-release-dates
   []
   (let [repo (git/load-repo "../proto-repl")]
     (->> (git/git-log repo)
          (map #(gitq/commit-info repo %))
          (filter #(str/starts-with? (:message %) "Prepare "))
          (map parse-git-commit))))


 (def release-dates
   (reverse (get-version-release-dates))))
