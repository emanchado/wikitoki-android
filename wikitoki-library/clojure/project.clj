(defproject wikitoki "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [markdown-clj "0.9.44"]
                 [local/fake-android-context "0.1.0"]
                 [midje "1.6.3"]]
  :aot [wikitoki.WikiToki]
  :main wikitoki.main)
