# Clj-lens: Nested data structure querying and updating

Have you ever wanted to update a map in an array in a map? Do you ever
want to extract several values out of a similar data structure? Either
situation is a pain. And it's ugly, nesting all of those access and
update functions into a pile a deeply-indented code. Clj-lens aspires
to help you solve these problems in an Clojure-idiomatic way.

Obligatory incantation to keep the nannies at bay: Updating and even
accessing deeply nested data may be a bad code smell. It may also
represent an anti-pattern. Let's all write beautiful code, people.

## Installation

Artifacts are published on [Clojars][1]. 

![latest version][2]

## Overview

Clj-lens allows the retrieval and updating of data structures through
path specifications that allow maps, vectors, sets, and conceivably
any type of compound data structures.

Clj-lens defines a protocol `AFocusable` containing two methods: `get`
and `update*`. The `update*` method should not be called directly but
through the `update` function, which allows multiple updates to be
applied to a data structure. The `get-many` function can be used to
retrieve a sequence of values based on zero or more path
specifications. Finally, a `let` macro is defined that allows you to
directly bind values to names.

Looking at the defined names described above, it should be pretty
clear that you should not `:refer` to them but should instead require
the `clj-lens.core` namespace using a convenient name such as `lens`.

Looking at the examples should make all or this transparently
clear. If not, please complain to me by opening a Github issue.

## Note on updating sets

If a set member is updated, the return value of the update function is
examined. If it is truthy, the returned value is conj'd unto the
set--after the value associated with the path specification element
value is disj'd from the set. If the returned value is equal to the
path specification element value, the original set is changed. If the
returned value is not truthy, the path specification element value is
disj'd from the set.

## Examples

```clojure

(require '[clj-lens.core :as lens])

(def m {:a 0, :b 1, :c [41 "Foocar"]})
(def n {:r (range 10)})

(lens/get m [:c 1]) ;; => "Foocar"
(lens/get 42 [])    ;; => 42

(lens/update m [:c 1 3] (fn [_] \b)) ;; => {:a 0, :b 1, :c [41 "Foobar"]}
(lens/update 0 [] #(+ % 42))         ;; => 42

(lens/update n [:r] #(map (partial * 10) %))
;; => {:r (0 10 20 30 40 50 60 70 80 90)} ; Still lazy

(lens/get-many m [:a] [:c 1 0])    ;; => (0 \F)
(lens/get-many m [:a] [:c 1 0] []) ;; => (0 \F {:a 0, :b 1, :c [41 "Foocar"]})

(lens/let m
    [p [:a]
     q [:c 1 0]
     :as r]
  (list p q r))
;; => (0 \F {:a 0, :b 1, :c [41 "Foocar"]}) ; Very similar to above

(lens/let {:a [0 1 2]}
    [[zero one two] [:a]]
  (list zero one two))
;; => (0 1 2)

(lens/update
 m
 [:a] dec
 [:b] inc
 [:d :i] (fn [_] #{:clubs :diamonds :hearts :spades}))
;; =>
;;
;; {:a -1,
;;  :b 2,
;;  :c [41 "Foocar"],
;;  :d {:i #{:spades :diamonds :clubs :hearts}}}

```

[1]: https://clojars.org/edw/clj-lens
[2]: https://clojars.org/edw/clj-lens/latest-version.svg
