(ns clojure-http-basics.threading
  (:require [clojure.string :as str]))


;; nested calls

(->> [1 2 3 4 5 6]
     (map inc)
     (filter (fn [x] (> x 3)))
     (mapcat (fn [x] (repeat x x)))
     (frequencies))

(->> [1 2 3 4 5 6]
     (map inc)
     (filter (fn [x] (> x 3)) )
     (mapcat (fn [x] (repeat x x)))
     (frequencies))

(dissoc (update (assoc {:a 1} :b 2) :b inc) :a)

(-> {:a 1}
    (assoc :b 2)
    (update :b inc)
    (dissoc :a))

(let [result (-> {:a 1}
                 (assoc :b 2)
                 (update :b inc)
                 (dissoc :a))]
  (->> result
       (map (fn [[k v]]
              (str k v)))))

(->> (-> {:a 1}
         (assoc :b 2)
         (update :b inc)
         (dissoc :a))
     (map (fn [[k v]]
            (str k v))))

(defn could-be-nil
  [s]
  (if (= s "abc")
    nil
    s))

(let [result-that-could-be-nil (some-> "ab1c"
                                       (could-be-nil)
                                       (str/capitalize))]
  result-that-could-be-nil)


(let [step-1? false
      step-2? true
      step-3? true
      step-1-fn (fn [m] (update m :steps conj :step-1))
      step-2-fn (fn [m] (update m :steps conj :step-2))
      step-3-fn (fn [m] (update m :steps conj :step-3))
      ]

  (cond->> {:steps []}
           step-1? (step-1-fn)
           step-2? (step-2-fn)
           step-3? (step-3-fn)))


(defn bad-fn
  [prefix s]
  (str prefix "-" s))

(defn fix-bad-fn
  [s prefix]
  (bad-fn prefix s))

(-> "asdf"
    (str/capitalize)
    (fix-bad-fn "p"))



























; thread first ->

;; thread last ->>

;; prevent NPE (some->, some->>)


