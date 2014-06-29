(ns midje.robohydra
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

;; WikiToki API mock server configuration
(def ^:dynamic *robohydra-url* "http://localhost:3000")

(defn- manipulate-scenario [plugin-name scenario-name new-props]
  (http/post (str *robohydra-url* "/robohydra-admin/rest/plugins/" plugin-name "/scenarios/" scenario-name) {:form-params new-props}))

(defn activate-scenario [plugin-name scenario-name]
  (manipulate-scenario plugin-name scenario-name {:active "true"}))

(defn deactivate-scenario [plugin-name scenario-name]
  (manipulate-scenario plugin-name scenario-name {:active "false"}))

(defmacro with-scenario [plugin-name scenario-name & body]
  `(do
     (activate-scenario ~plugin-name ~scenario-name)
     (let [result# (do ~@body)]
       (deactivate-scenario ~plugin-name ~scenario-name)
       result#)))

(defn fetch-scenario-result [plugin-name scenario-name]
  (let [robohydra-response (http/get (str *robohydra-url* "/robohydra-admin/rest/test-results"))
        all-test-results (json/read-str (:body robohydra-response))
        scenario-test-results (-> all-test-results
                                  (get plugin-name)
                                  (get scenario-name))]
    (and (empty? (get scenario-test-results "failures"))
         (not (empty?  (get scenario-test-results "passes"))))))

(defmacro results-with-scenario [plugin-name scenario-name & body]
  `(do
     (activate-scenario ~plugin-name ~scenario-name)
     ~@body
     (deactivate-scenario ~plugin-name ~scenario-name)
     (fetch-scenario-result ~plugin-name ~scenario-name)))
