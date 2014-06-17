WikiToki library
================

This is a JVM library to access the WikiToki API and provide an easy-to-use
interface for Android applications. The goal is to implement as much as
possible in this library, leaving only UI to the Android app.

It's written in Clojure, although the API is a class with objects that look
"normal" in Java.


The java/ directory is simply a mock of the android.content.Context class so
this library can be development without depending on Android.
