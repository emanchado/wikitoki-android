(ns wikitoki.WikiToki-test
  (:require [clojure.java.io :refer [make-parents file delete-file]]
            [midje.sweet :refer :all]
            [wikitoki.WikiToki :refer :all]
            [clojure.string :as s])
  (:import android.content.Context))

(def wiki-toki (map->WikiTokiRecord {:state (atom {:ctx (Context.)})}))

(defn delete-recursively [fname]
  (let [f (file fname)]
    (if (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-recursively child)))
    (delete-file f true)))

(defn setup-dirs []
  (delete-recursively "pages")
  (.mkdirs (file "pages")))

(with-state-changes [(before :facts (setup-dirs))]
  (fact "Can create WikiToki objects"
        wiki-toki => (fn [r] r))

  (fact "There are no pages by default"
        (-doesLocalPageExist wiki-toki "WikiIndex") => false)

  (fact "Can create a simple page and read it back"
        (let [wiki-text "This is a simple WikiPage without much *formatting*."]
          (-writeLocalPage wiki-toki "WikiIndex" wiki-text)
          (-readLocalPage wiki-toki "WikiIndex") => wiki-text))

  (fact "Basic rendering works"
        (let [wiki-text "Some WikiLink first, then **bold**"]
          (-writeLocalPage wiki-toki "WikiIndex" wiki-text)
          (-renderLocalPage wiki-toki "WikiIndex") =>
          (fn [rendered-page-text]
            (and (re-find #"<p>" rendered-page-text)
                 (re-find #"wikitoki://" rendered-page-text)))))

  (fact "Non-WikiLinks are NOT converted to wiki links"
        (let [wiki-text "Some notWikiLink to test"]
          (-writeLocalPage wiki-toki "WikiIndex" wiki-text)
          (-renderLocalPage wiki-toki "WikiIndex") =>
          (fn [rendered-page-text]
            (not (re-find #"wikitoki://" rendered-page-text))))))
