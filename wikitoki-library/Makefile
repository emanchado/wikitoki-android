# Update this to read from project.clj
VERSION=$(shell head -n 1 clojure/project.clj | sed 's/.*"\(.*\)"/\1/')

../app/libs/wikitoki-$(VERSION)-standalone.jar: clojure/target/wikitoki-$(VERSION)-standalone.jar
	mkdir zip-tmp
	cd zip-tmp && unzip -q ../$< && rm -rf com/google/ goog/ org/mozilla/ org/json/ rhino_ast/ META-INF/maven/fake-android-context/ META-INF/leiningen/fake-android-context/ android/ cljs/
	rm -f $@ && cd zip-tmp && zip -9rq ../$@ .
	rm -rf zip-tmp

clojure/target/wikitoki-$(VERSION)-standalone.jar: clojure/src/wikitoki/*.clj
	cd clojure && lein uberjar
