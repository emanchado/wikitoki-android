(ns wikitoki.WikiToki
  (:import android.content.Context
           java.io.File
           java.io.FileInputStream
           java.io.FileOutputStream)
  (:require [clojure.string :as s]
            [markdown.core :as md]
            [markdown.transformers :as mt]))

(gen-class :name wikitoki.WikiToki
           :init init
           :state state
           :main false
           :constructors {[android.content.Context] []}
           :methods [[readLocalPage [String] String]
                     [writeLocalPage [String String] void]
                     [renderLocalPage [String] String]
                     [doesLocalPageExist [String] boolean]])

;; This is just for usage from Clojure (testing)
(defrecord WikiTokiRecord [state])

(defn -init [^android.content.Context ctx]
  [[] (atom {:ctx ctx})])

(defn ctx [this]
  (@(.state this) :ctx))

(defn -readLocalPage [this ^String pageName]
  (let [input-dir (.getDir (ctx this) "pages" 0)
        input-file (File. input-dir pageName)
        input-stream (FileInputStream. input-file)
        buffer-size (* 2 1024 1024)
        buffer (byte-array buffer-size)
        bytes-read (.read input-stream buffer 0 buffer-size)]
    (String. buffer 0 bytes-read "UTF-8")))

(defn -writeLocalPage [this ^String pageName ^String contents]
  (let [output-dir (.getDir (ctx this) "pages" 0)
        output-file (File. output-dir pageName)
        output-stream (FileOutputStream. output-file)]
    (.write output-stream (.getBytes contents))
    (.close output-stream)))

(defn -doesLocalPageExist [this ^String pageName]
  (let [file-list (.list (.getDir (ctx this) "pages" 0))]
    (boolean (some #(= pageName %) file-list))))

(defn linkify-wiki-names [line state]
  [(s/replace line
              #"(!)?\b[A-Z][a-z0-9]+([A-Z][a-z0-9]*)+\b"
              "[$1](wikitoki://$1)")
   state])

(defn -renderLocalPage [this ^String pageName]
  (let [page-contents (-readLocalPage this pageName)]
    (md/md-to-html-string page-contents
                          :replacement-transformers (cons linkify-wiki-names mt/transformer-vector))))
