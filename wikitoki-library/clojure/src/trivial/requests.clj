(ns trivial.requests
  (:import java.io.OutputStreamWriter
           java.net.HttpURLConnection
           java.net.URL))

(defn get-request [^String url-string]
  (let [url (URL. url-string)
        conn (.openConnection url)]
    (.setRequestMethod conn "GET")
    (let [is (.getInputStream conn)
          buffer-size (* 2 1024 1024)
          buffer (byte-array buffer-size)
          bytes-read (loop [start 0]
                       (let [max-bytes (- buffer-size start)
                             nbytes (.read is buffer start max-bytes)]
                         (if (pos? nbytes)
                           (recur (+ nbytes start))
                           start)))]
      {:body (String. buffer 0 bytes-read "UTF-8")
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
          buffer-size (* 2 1024 1024)
          buffer (byte-array buffer-size)
          bytes-read (loop [start 0]
                       (let [max-bytes (- buffer-size start)
                             nbytes (.read is buffer start max-bytes)]
                         (if (pos? nbytes)
                           (recur (+ nbytes start))
                           start)))]
      {:body (String. buffer 0 bytes-read "UTF-8")
       :status (.getResponseCode conn)})))
