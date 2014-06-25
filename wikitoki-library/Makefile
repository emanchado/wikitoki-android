# Update this to read from project.clj
VERSION=$(shell head -n 1 clojure/project.clj | sed 's/.*"\(.*\)"/\1/')
WIKITOKI_JAR=../app/libs/wikitoki-$(VERSION)-standalone.jar
FAKE_ANDROID_JAR=java/target/fake-android-context-0.1.0-SNAPSHOT.jar

all: $(WIKITOKI_JAR) $(FAKE_ANDROID_JAR)


$(WIKITOKI_JAR): clojure/target/wikitoki-$(VERSION)-standalone.jar
	mkdir zip-tmp
	cd zip-tmp && unzip -q ../$< && rm -rf com/google/ goog/ org/mozilla/ org/json/ rhino_ast/ META-INF/maven/fake-android-context/ META-INF/leiningen/fake-android-context/ android/ cljs/
	rm -f $@ && cd zip-tmp && zip -9rq ../$@ .
	rm -rf zip-tmp

clojure/target/wikitoki-$(VERSION)-standalone.jar: clojure/src/wikitoki/*.clj
	cd clojure && lein uberjar


$(FAKE_ANDROID_JAR): java/src/android/content/Context.java
	cd java && lein jar && mvn install:install-file -Dfile=target/fake-android-context-0.1.0-SNAPSHOT.jar -DgroupId=local -DartifactId=fake-android-context -Dversion=0.1.0 -Dpackaging=jar
