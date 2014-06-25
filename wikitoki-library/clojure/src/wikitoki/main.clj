(ns wikitoki.main
  (:import android.content.Context
           wikitoki.WikiToki))

(defn -main [& args]
  (let [wikiToki (WikiToki. (Context.))]
    (println wikiToki)
    (.writeLocalPage wikiToki "WikiIndex" "WikiIndex - This is the beginning")
    (println (.readLocalPage wikiToki "WikiIndex"))
    (println (.renderLocalPage wikiToki "WikiIndex"))))
