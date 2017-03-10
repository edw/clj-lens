# Clj-lens: a tool for focusing on values

Have you ever wanted to update a map in an array in a map? Do you ever
want to extract several values out of a similar data structure? Either
situation is a pain. And it's ugly, nesting all of those access and
update functions into a pile a deeply-indented code. Clj-lens aspires
to help you solve these problems in an Clojure-idiomatic way.

Obligatory incantation to keep the nannies at bay: Updating and even
accessing deeply nested data may be a bad code smell. It may also
represent an anti-pattern. Let's all write beautiful code, people.

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
