(ns wikitoki.main
  (:import android.content.Context
           wikitoki.WikiToki)
  (:require [clojure.string :as s]))

(defn -main [& args]
  (let [wikiToki (WikiToki. (Context.))]
    (println wikiToki)
    (.writeLocalPage wikiToki "WikiIndex" "WikiIndex - This is the beginning")
    (println (str "Pages -> " (s/join ", " (.listLocalPages wikiToki))))
    (println (.readLocalPage wikiToki "WikiIndex"))
    (println (.renderLocalPage wikiToki "WikiIndex"))))
