(ns wikitoki.textmerger-test
  (:require [wikitoki.textmerger :refer [three-way-merge]]
            [midje.sweet :refer :all]))


(fact "New local version wins when no server changes"
      (three-way-merge "foo\nbar" "foo\nbar" "bar\nnew line")
      => "bar\nnew line")

(fact "Server version wins when no local changes"
      (three-way-merge "foo\nbar" "foo\nqux" "foo\nqux")
      => "foo\nbar")

(fact "Adding in different parts should get all lines added"
      (three-way-merge "server\nfoo\nbar" "foo\nbar" "foo\nbar\nlocal")
      => "server\nfoo\nbar\nlocal")
