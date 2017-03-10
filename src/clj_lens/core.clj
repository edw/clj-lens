(ns clj-lens.core
  (:refer-clojure :exclude [get let update]))

(defprotocol AFocusable
  "Something which can be focused upon"
  (get [x spec] "Get value")
  (update* [x spec f] "Update value"))

(extend clojure.lang.LongRange
  AFocusable
  {:get
   (fn [x spec]
     (if (empty? spec)
       x
       (get (nth x (first spec)) (rest spec))))
   :update*
   (fn [x spec f]
     (if (empty? spec)
       (f x)
       (map-indexed
        (fn [i el]
          (if (= i (first spec))
            (update* el (rest spec) f)
            el))
        x)))})

(extend clojure.lang.IPersistentMap
  AFocusable
  {:get
   (fn [m spec]
     (if (empty? spec)
       m
       (get (clojure.core/get m (first spec)) (rest spec))))
   :update*
   (fn [m spec f]
     (if (empty? spec)
       (f m)
       (clojure.core/update m (first spec) #(update* % (rest spec) f))))})

(extend clojure.lang.APersistentSet
  AFocusable
  {:get
   (fn [s spec]
     (if (empty? spec)
       s
       (get (clojure.core/get s (first spec)) (rest spec))))})

(defn- replace-at [s i f]
  (str (subs s 0 i) (f (.charAt s i)) (subs s (inc i))))

(extend java.lang.String
  AFocusable
  {:get
   (fn [s spec]
     (cond (empty? spec)
           s
           (= 1 (count spec))
           (.charAt s (first spec))
           :else
           (throw (Exception. "Cannot focus beyond a character"))))
   :update*
   (fn [s spec f]
     (cond (empty? s)
           (f s)
           (= 1 (count spec))
           (replace-at s (first spec) f)
           :else
           (throw (Exception. "Cannot focus beyond a character"))))})

(extend clojure.lang.IPersistentVector
  AFocusable
  {:get
   (fn [coll spec]
     (cond (empty? spec)
           coll
           (= 1 (count spec))
           (clojure.core/get coll (first spec))
           :else
           (get (clojure.core/get coll (first spec)) (rest spec))))
   :update*
   (fn [coll spec f]
     (if (empty? spec)
       (f coll)
       (assoc coll
              (first spec)
              (update* (clojure.core/get coll (first spec)) (rest spec) f))))})

(extend nil
  AFocusable
  {:get
   (fn [x spec]
     nil)
   :update*
   (fn [x spec f]
     (if (empty? spec)
       (f x)
       (assoc {} (first spec) (update* x (rest spec) f))))})

(extend java.lang.Object
  AFocusable
  {:get
   (fn [x spec]
     (if (not-empty spec)
       (throw (Exception. "Object does not accept non-nil spec"))
       x))
   :update*
   (fn [x spec f]
     (if (not-empty spec)
       (throw (Exception. "Object does not accept non-nil spec"))
       (f x)))})

(defn update [m & specfs]
  (if (not-empty specfs)
    (clojure.core/let [m (update* m (first specfs) (second specfs))]
      (apply update m (nthrest specfs 2)))
    m))

(defn get-many [m & specs]
  (map #(get m %) specs))

(defn- binding-names [bindings]
  (map
   (fn [b] (if (= :as (first b)) (second b) (first b)))
   (partition 2 bindings)))

(defn- binding-specs [bindings]
  (map
   (fn [b] (if (= :as (first b)) [] (second b)))
   (partition 2 bindings)))

(defmacro let [x bindings & body]
  (clojure.core/let [names (binding-names bindings)
                     specs (binding-specs bindings)]
    `(clojure.core/let [[~@names] (get-many ~x ~@specs)]
       ~@body)))
