(ns wikitoki.WikiToki-test
  (:require [clj-http.client :as http]
            [clojure.string :as s]
            [midje.sweet :refer :all]
            [wikitoki.WikiToki :refer :all])
  (:import android.content.Context))

(def ^:dynamic *robohydra-url* "http://localhost:3000")

(defmacro with-scenario [name & body]
  `(do
     (http/post (str *robohydra-url* "/robohydra-admin/rest/plugins/wikitoki-api/scenarios/" ~name) {:form-params {:active "true"}})
     (let [result# (do ~@body)]
       (http/post (str *robohydra-url* "/robohydra-admin/rest/plugins/wikitoki-api/scenarios/" ~name) {:form-params {:active "false"}})
       result#)))

(def wiki-toki (map->WikiTokiRecord {:state (atom {:ctx (Context.)
                                                   :wiki-url "http://localhost:3000"})}))

(with-state-changes [(before :facts (-wipe wiki-toki))]
  (fact "Can create WikiToki objects"
        wiki-toki => (fn [r] r))

  (fact "There are no pages by default"
        (-doesLocalPageExist wiki-toki "WikiIndex") => false)

  (fact "Can create a simple page and read it back"
        (let [wiki-text "This is a simple WikiPage without much *formatting*."]
          (-writeLocalPage wiki-toki "WikiIndex" wiki-text)
          (-readLocalPage wiki-toki "WikiIndex")
          => wiki-text))

  (fact "Created pages start to exist"
        (-writeLocalPage wiki-toki "WikiIndex" "Whatever")
        (-doesLocalPageExist wiki-toki "WikiIndex")
        => true)

  (fact "Basic rendering works"
        (let [wiki-text "Some WikiLink first, then **bold**"]
          (-writeLocalPage wiki-toki "WikiIndex" wiki-text)
          (-renderLocalPage wiki-toki "WikiIndex")
          => (fn [rendered-page-text]
               (and (re-find #"<p>" rendered-page-text)
                    (re-find #"wikitoki://" rendered-page-text)))))

  (fact "Non-WikiLinks are NOT converted to wiki links"
        (let [wiki-text "Some notWikiLink to test"]
          (-writeLocalPage wiki-toki "WikiIndex" wiki-text)
          (-renderLocalPage wiki-toki "WikiIndex")
          => (fn [rendered-page-text]
               (not (re-find #"wikitoki://" rendered-page-text)))))

  (fact "Can get the list of local pages"
        (-writeLocalPage wiki-toki "WikiIndex" "Some text")
        (-writeLocalPage wiki-toki "AnotherPage" "More text")
        (sort (-listLocalPages wiki-toki))
        => ["AnotherPage" "WikiIndex"])

  (fact "Can receive pages from the API"
        (with-scenario "twoPages"
          (-fetchRemotePages wiki-toki)
          (sort (-listLocalPages wiki-toki)))
        => ["RoboHydra" "WikiIndex"])

  (fact "Takes local changes if the server hasn't changed"
        (let [orig-page "This is the index from RoboHydra."
              local-page (str orig-page "\nThis is a second line")]
          (with-scenario "twoPages"
            (writePageFromServer wiki-toki "WikiIndex" orig-page)
            (-writeLocalPage wiki-toki "WikiIndex" local-page)
            (-fetchRemotePages wiki-toki)
            (-readLocalPage wiki-toki "WikiIndex"))
          => local-page)))

;; SENDS local changes back to the server
;; Receiving pages from the API does not remove new local pages
;; Test that a 304 doesn't delete the pages
