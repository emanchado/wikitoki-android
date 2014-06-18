(ns wikitoki.core
  (:import android.content.Context)
  (:require [clojure.string :as s]
            [markdown.core :as md]
            [markdown.transformers :as mt]))

(gen-class :name wikitoki.core.WikiToki
           :init init
           :state state
           :main false
           :constructors {[android.content.Context] []}
           :methods [[readLocalPage [String] String]
                     [writeLocalPage [String String] void]
                     [renderLocalPage [String] String]])

(defn -init [^android.content.Context ctx]
  [[] (atom {:ctx ctx})])

(defn ctx [this]
  (@(.state this) :ctx))

(defn -readLocalPage [this ^String pageName]
  (let [input-file (.openFileInput (ctx this) pageName)
        buffer-size (* 2 1024 1024)
        buffer (byte-array buffer-size)
        bytes-read (.read input-file buffer 0 buffer-size)]
    (String. buffer 0 bytes-read "UTF-8")))

(defn -writeLocalPage [this ^String pageName ^String contents]
  (let [output-file (.openFileOutput (ctx this) pageName android.content.Context/MODE_PRIVATE)]
    (.write output-file (.getBytes contents))
    (.close output-file)))

(defn linkify-wiki-names [line state]
  [(s/replace line #"(WikiIndex)" "[$1](wikitoki://$1)") state])

(defn -renderLocalPage [this ^String pageName]
  (let [page-contents (.readLocalPage this pageName)]
    (md/md-to-html-string page-contents
                          :replacement-transformers (cons linkify-wiki-names mt/transformer-vector))))
