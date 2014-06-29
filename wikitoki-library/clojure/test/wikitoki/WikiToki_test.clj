(ns wikitoki.WikiToki-test
  (:require [clojure.string :as s]
            [midje.robohydra :refer :all]
            [midje.sweet :refer :all]
            [wikitoki.WikiToki :refer :all])
  (:import android.content.Context))

;; WikiToki object we'll use for the tests
(def wiki-toki
  (map->WikiTokiRecord {:state (atom {:ctx (Context.)
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
                    (re-find #"wikitoki://WikiLink" rendered-page-text)))))

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
        (with-scenario "wikitoki-api" "two-pages"
          (-synchronizePages wiki-toki)
          (sort (-listLocalPages wiki-toki)))
        => ["RoboHydra" "WikiIndex"])

  (fact "Takes local changes if the server hasn't changed"
        (let [orig-page "This is the index from RoboHydra."
              local-page (str orig-page "\nThis is a second line")]
          (with-scenario "wikitoki-api" "two-pages"
            (writePageFromServer wiki-toki "WikiIndex" orig-page)
            (-writeLocalPage wiki-toki "WikiIndex" local-page)
            (-synchronizePages wiki-toki)
            (-readLocalPage wiki-toki "WikiIndex"))
          => local-page))

  (fact "Sends local changes if the server hasn't changed"
        (let [orig-page "This is the index from RoboHydra."
              local-page (str orig-page "\nThis is a second line")]
          (results-with-scenario "wikitoki-api" "send-local-changes"
            (writePageFromServer wiki-toki "WikiIndex" orig-page)
            (-writeLocalPage wiki-toki "WikiIndex" local-page)
            (-synchronizePages wiki-toki))
          => true))

  (fact "Receiving pages from the API does not remove new local pages"
        (with-scenario "wikitoki-api" "only-wikiindex"
          (writePageFromServer wiki-toki "WikiIndex" "First page")
          (-writeLocalPage wiki-toki "WikiIndex" "First page")
          (-writeLocalPage wiki-toki "NewLocalPage" "New page!")
          (-synchronizePages wiki-toki)
          (-doesLocalPageExist wiki-toki "NewLocalPage"))
        => true)

  (fact "New local pages are sent back to the server"
        (results-with-scenario "wikitoki-api" "send-new-local-page"
          (writePageFromServer wiki-toki "WikiIndex" "First page")
          (-writeLocalPage wiki-toki "WikiIndex" "First page")
          (-writeLocalPage wiki-toki "NewLocalPage" "New page!")
          (-synchronizePages wiki-toki))
        => true)

  (fact "Receiving a 304 Not Modified from the server doesn't delete local pages"
        (with-scenario "wikitoki-api" "not-modified-since"
          (writePageFromServer wiki-toki "WikiIndex" "First page")
          (-writeLocalPage wiki-toki "WikiIndex" "First page")
          (writePageFromServer wiki-toki "AnotherPage" "Another page")
          (-writeLocalPage wiki-toki "AnotherPage" "Another page")
          (-synchronizePages wiki-toki)
          (sort (-listLocalPages wiki-toki)))
        => ["AnotherPage" "WikiIndex"]))
