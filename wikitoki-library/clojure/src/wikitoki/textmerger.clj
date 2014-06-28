(ns wikitoki.textmerger
  (:require [clojure.string :as s]))

(defn new-lines-in [source reference]
  (let [reference-line-set (set (s/split reference #"\n"))
        source-lines (s/split source #"\n")
        new-source-lines (filter #(not (contains? reference-line-set %))
                                 source-lines)]
    (s/join "\n" new-source-lines)))

(defn three-way-merge [server-version initial-local-version new-local-version]
  (let [new-local-lines (new-lines-in new-local-version initial-local-version)]
    (cond
     (= initial-local-version server-version) new-local-version
     (= initial-local-version new-local-version) server-version
     :else (str server-version "\n" new-local-lines))))
