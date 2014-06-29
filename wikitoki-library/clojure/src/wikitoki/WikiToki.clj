(ns wikitoki.WikiToki
  (:import android.content.Context
           java.io.File
           java.io.FileInputStream
           java.io.FileOutputStream)
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [clojure.string :as s]
            [markdown.core :as md]
            [markdown.transformers :as mt]
            [wikitoki.textmerger :refer [three-way-merge]]
            [clojure.java.io :refer [make-parents file delete-file]]))

(gen-class :name wikitoki.WikiToki
           :init init
           :state state
           :main false
           :constructors {[android.content.Context] []}
           :methods [[readLocalPage [String] String]
                     [writeLocalPage [String String] void]
                     [renderLocalPage [String] String]
                     [doesLocalPageExist [String] boolean]
                     [listLocalPages [] "[Ljava.lang.String;"]
                     [synchronizePages [] void]
                     [wipe [] void]])

;; This is just for usage from Clojure (testing)
(defrecord WikiTokiRecord [state])

(defn -init [^android.content.Context ctx ^String wiki-url]
  [[] (atom {:ctx ctx, :wiki-url wiki-url})])

(defn ctx [this]
  (@(.state this) :ctx))

(defn wiki-url [this]
  (@(.state this) :wiki-url))

(defn -readLocalPage [this ^String name]
  (let [input-dir (.getDir (ctx this) "pages" 0)
        input-file (File. input-dir name)
        buffer-size (* 2 1024 1024)
        buffer (byte-array buffer-size)]
    (try
      (let [ input-stream (FileInputStream. input-file)
            bytes-read (.read input-stream buffer 0 buffer-size)]
        (String. buffer 0 bytes-read "UTF-8"))
      (catch java.io.FileNotFoundException e
        nil))))

(defn -writeLocalPage [this ^String name ^String contents]
  (let [output-dir (.getDir (ctx this) "pages" 0)
        output-file (File. output-dir name)
        output-stream (FileOutputStream. output-file)]
    (.write output-stream (.getBytes contents))
    (.close output-stream)))

(defn linkify-wiki-names [line state]
  [(s/replace line
              #"(!)?\b([A-Z][a-z0-9]+([A-Z][a-z0-9]*)+)\b"
              "[$2](wikitoki://$2)")
   state])

(defn -renderLocalPage [this ^String name]
  (let [page-contents (-readLocalPage this name)]
    (md/md-to-html-string page-contents
                          :replacement-transformers (cons linkify-wiki-names mt/transformer-vector))))

(defn -listLocalPages [this]
  (into-array String (.list (.getDir (ctx this) "pages" 0))))

(defn -doesLocalPageExist [this ^String name]
  (boolean (some #(= name %) (-listLocalPages this))))

(defn readPageFromServer [this ^String name]
  (let [input-dir (.getDir (ctx this) "orig-pages" 0)
        input-file (File. input-dir name)
        buffer-size (* 2 1024 1024)
        buffer (byte-array buffer-size)]
    (try
      (let [input-stream (FileInputStream. input-file)
            bytes-read (.read input-stream buffer 0 buffer-size)]
        (String. buffer 0 bytes-read "UTF-8"))
      (catch java.io.FileNotFoundException e
        nil))))

(defn writePageFromServer [this ^String name ^String contents]
  (let [output-dir (.getDir (ctx this) "orig-pages" 0)
        output-file (File. output-dir name)
        output-stream (FileOutputStream. output-file)]
    (.write output-stream (.getBytes contents))
    (.close output-stream)))

(defn sendPageToServer [this ^String name]
  (let [contents (readPageFromServer this name)]
    (http/post (str (wiki-url this) "/api/pages/" name)
               {:body (json/write-str {:name name
                                       :contents contents})})))

(defn processPageListResponse [this response-body]
  (let [response-object (json/read-str response-body)
        pages (get response-object "pages")]
    (doseq [[page-name page-text] pages]
      (let [old-server-page (readPageFromServer this page-name)
            local-page (-readLocalPage this page-name)
            final-text (three-way-merge (or page-text "")
                                        (or old-server-page "")
                                        (or local-page ""))]
        (writePageFromServer this page-name final-text)
        (-writeLocalPage this page-name final-text)
        (sendPageToServer this page-name)))
    (let [server-page-set (set (map first pages))]
      (doseq [new-page-name (filter #(not (contains? server-page-set %))
                                    (-listLocalPages this))]
        (let [new-page-contents (-readLocalPage this new-page-name)]
          (writePageFromServer this new-page-name new-page-contents)
          (sendPageToServer this new-page-name))))))

(defn -synchronizePages [this]
  (try
    (let [response @(http/get (str (wiki-url this) "/api/pages"))
          status-code (:status response)]
      (if (= status-code 200)
        (processPageListResponse this (:body response))
        (println (str "Ignoring server response with status " status-code))))
    (catch Exception e
      ;; This is a slingshot exception, hence the complicated way to
      ;; access the status code
      (when (not= (:status (:object (.data e))) 304)
        (throw e)))))

(defn -wipe [this]
  (letfn [(delete-recursively [fname]
            (let [f (file fname)]
              (if (.isDirectory f)
                (doseq [child (.listFiles f)]
                  (delete-recursively child)))
              (delete-file f true)))]
    (delete-recursively "pages")
    (delete-recursively "orig-pages")
    (.mkdirs (file "pages"))
    (.mkdirs (file "orig-pages"))))
