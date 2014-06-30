(ns trivial.requests
  (:import java.io.OutputStreamWriter
           java.io.InputStreamReader
           java.io.BufferedReader
           java.net.URL))

(defn get-request [^String url-string]
  (let [url (URL. url-string)
        conn (.openConnection url)]
    (.setRequestMethod conn "GET")
    (let [is (.getInputStream conn)
          buffered-reader (BufferedReader. (InputStreamReader. is))
          body (loop [body ""]
                 (let [line (.readLine buffered-reader)]
                   (if (nil? line)
                     (do
                       (.close buffered-reader)
                       body)
                     (recur (str body "\n" line)))))]
      {:body body
       :status (.getResponseCode conn)})))

(defn post-request [^String url-string ^String body]
  (let [url (URL. url-string)
        conn (.openConnection url)]
    (.setRequestMethod conn "POST")
    (.setDoOutput conn true)
    (let [os (.getOutputStream conn)
          osw (OutputStreamWriter. os)]
      (.write osw body)
      (.flush osw)
      (.close osw))
    (let [is (.getInputStream conn)
          buffered-reader (BufferedReader. (InputStreamReader. is))
          body (loop [body ""]
                 (let [line (.readLine buffered-reader)]
                   (if (nil? line)
                     (do
                       (.close buffered-reader)
                       body)
                     (recur (str body "\n" line)))))]
      {:body body
       :status (.getResponseCode conn)})))
