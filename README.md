# Clj-lens: a tool for focusing on values

## Items for consideration

* Implement insert
* Implement remove
* Implement prepend
* Implement append
* Implement subsequence
* Implement concatenate
* Identify which operations are primitive and which are derived

## Examples

```clojure

(require '[clj-lens.core :as lens])

(def m {:a 0, :b 1, :c [41 "Foocar"]})

(lens/get m [:c 1]) ;; => "Foocar"
(lens/get 42 [])    ;; => 42

(lens/update m [:c 1 3] (fn [_] \b)) ;; => {:a 0, :b 1, :c [41 "Foobar"]}
(lens/update 0 [] #(+ % 42)) ;; => 42

(lens/get-many m [:a] [:c 1 0]) ;; => (0 \F)
(lens/get-many m [:a] [:c 1 0] []) ;; => (0 \F {:a 0, :b 1, :c [41 "Foocar"]})

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
