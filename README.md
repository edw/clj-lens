# Clj-lens: a tool for focusing on values

## Todo list

* Implement insert
* Implement remove
* Implement prepend
* Implement append

## Examples

```clojure

(require '[clj-lens.core :as lens])

(def m {:a 0, :b 1, :c [41 "Foocar"]})

(lens/get m [:c 1])
;; => "Foocar"

(lens/update m [:c 1 3] (fn [_] \b))
;; => {:a 0, :b 1, :c [41 "Foobar"]}

(lens/get-many m [:a] [:c 1 0])
;; => (0 \F)

(lens/update-many
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
