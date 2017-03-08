(ns lens.core)

(defprotocol AFocusable
  "Something which can be focused upon"
  (fget [x spec] "Get value")
  (fupdate [x spec f] "Update value"))

(extend clojure.lang.LongRange
  AFocusable
  {:fget
   (fn [x [spec-el & spec-rest]]
     (fget (nth x spec-el) spec-rest))})

(extend clojure.lang.IPersistentMap
  AFocusable
  {:fget (fn [m [spec-el & spec-rest]]
           (fget (get m spec-el) spec-rest))
   :fupdate (fn [m [spec-el & spec-rest] f]
              (if (not-empty spec-rest)
                (update m spec-el #(fupdate % spec-rest f))
                (update m spec-el f)))})

(extend java.lang.String
  AFocusable
  {:fget
   (fn [s spec]
     (cond (empty? spec)
           s
           (= 1 (count spec))
           (.charAt s (first spec))
           :else
           (throw (Exception. "Cannot focus beyond a character"))))
   :fupdate
   (fn [s spec f]
     (cond (empty? s)
           (f s)
           (= 1 (count spec))
           (let [i (first spec)]
             (str (subs s 0 i) (f (.charAt s i)) (subs s (inc i))))
           :else
           (throw (Exception. "Cannot focus beyond a character"))))})

(extend clojure.lang.IPersistentVector
  AFocusable
  {:fget
   (fn [coll [spec-el spec-rest]]
     (fget (get coll spec-el) spec-rest))
   :fupdate
   (fn [coll [spec-el spec-rest] f]
     (if (not-empty spec-rest)
       (assoc coll spec-el (fupdate (get coll spec-el) spec-rest f))
       (assoc coll spec-el (f (get coll spec-el)))))})

(extend java.lang.Long
  AFocusable
  {:fget
   (fn [x spec] (if (not-empty spec)
                 (throw (Exception. "Long does not accept non-nil spec"))
                 x))
   :fupdate
   (fn [x spec f] (if (not-empty spec)
                   (throw (Exception. "Long does not accept non-nil spec"))
                   (f x)))})
