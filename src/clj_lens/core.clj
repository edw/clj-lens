(ns clj-lens.core
  (:refer-clojure :exclude [get update]))

(defprotocol AFocusable
  "Something which can be focused upon"
  (get [x spec] "Get value")
  (update* [x spec f] "Update value"))

(extend clojure.lang.LongRange
  AFocusable
  {:get
   (fn [x [spec-el & spec-rest]]
     (get (nth x spec-el) spec-rest))})

(extend clojure.lang.IPersistentMap
  AFocusable
  {:get
   (fn [m [spec-el & spec-rest]]
     (get (clojure.core/get m spec-el) spec-rest))
   :update*
   (fn [m [spec-el & spec-rest] f]
     (if (not-empty spec-rest)
       (clojure.core/update m spec-el #(update* % spec-rest f))
       (clojure.core/update m spec-el f)))})

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
           (let [i (first spec)]
             (replace-at s i f))
           :else
           (throw (Exception. "Cannot focus beyond a character"))))})

(extend clojure.lang.IPersistentVector
  AFocusable
  {:get
   (fn [coll specs]
     (cond (empty? specs)
           coll
           (= 1 (count specs))
           (clojure.core/get coll (first specs))
           :else
           (get (clojure.core/get coll (first specs)) (rest specs))))
   :update*
   (fn [coll [spec-el & spec-rest] f]
     (if (not-empty spec-rest)
       (assoc coll spec-el
              (update* (clojure.core/get coll spec-el) spec-rest f))
       (assoc coll spec-el (f (clojure.core/get coll spec-el)))))})

(extend nil
  AFocusable
  {:get
   (fn [x specs]
     nil)
   :update*
   (fn [x [spec-el & spec-rest] f]
     (if (not-empty spec-rest)
       (assoc {} spec-el (update* x spec-rest f))
       (assoc {} spec-el (f x))))})

(extend java.lang.Long
  AFocusable
  {:get
   (fn [x spec]
     (if (not-empty spec)
       (throw (Exception. "Long does not accept non-nil spec"))
       x))
   :update*
   (fn [x spec f]
     (if (not-empty spec)
       (throw (Exception. "Long does not accept non-nil spec"))
       (f x)))})

(defn update [m & specfs]
  (if (not-empty specfs)
    (let [m (update* m (first specfs) (second specfs))]
      (apply update m (nthrest specfs 2)))
    m))

(defn get-many [m & specs]
  (map #(get m %) specs))
