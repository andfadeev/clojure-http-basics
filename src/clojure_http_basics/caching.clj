(ns clojure-http-basics.caching
  (:require [cheshire.core :as json]
            [clojure.core.cache.wrapped :as cache]
            [com.stuartsierra.component :as component]
            [clojure.core.memoize :as memoize]))


;; `clojure.core.cache`

(defn ^{:clojure.core.memoize/args-fn rest}
  get-pokemon-name-by-id
  [dependencies id]
  (let [url (str "https://pokeapi.co/api/v2/pokemon-species/" id)]
    (println "Calling for id:" id)
    (-> (slurp url)
        (json/decode true)
        :name)))

(def dependencies {:datasource {}
                   :other-component {}})


(def pokemon-name-by-id-ttl
  (memoize/ttl
    #'get-pokemon-name-by-id
    :ttl/threshold 3000))


(comment
  (memoize/snapshot pokemon-name-by-id-ttl)
  (time (pokemon-name-by-id-ttl dependencies 100)))






(comment
  ;; #1 `clojure.core/memoize` function

  ;; Pokemon API: https://pokeapi.co/api/v2/pokemon-species/
  (defn pokemon-name-by-id
    [id]
    (let [url (str "https://pokeapi.co/api/v2/pokemon-species/" id)]
      (println "Calling:" url)
      (-> (slurp url)
          (json/decode true)
          :name)))

  (def pokemon-name-by-id-memo
    (memoize pokemon-name-by-id))

  (comment
    (time (pokemon-name-by-id-memo 1000))
    )

  ;; By default, the entire argument list is used as a key into the cached results. Sometimes you will want to cache a function where one or more of its arguments don't affect the results directly and you would rather ignore them from a cache key point of view.


  ;; Note#1: concurrent call can still trigger multiple executions of the body
  ;; note that here all args are a part of the key, so be careful
  ;; note that internal atom is never cleaned, so use wisely
  ;; with all these limitations sometimes you'll need to upgrade


  ;; #2 clojure.core.cache - most flexible, cache could be shared and also managed by Component or Integrant

  ;; different types: https://github.com/clojure/core.memoize/blob/master/docs/Using.md

  (def fifo-cache (cache/fifo-cache-factory {} :threshold 3))

  (defn pokemon-name-by-id-v2
    [dependencies id]
    (let [url (str "https://pokeapi.co/api/v2/pokemon-species/" id)]
      (println "Calling:" url)
      (-> (slurp url)
          (json/decode true)
          :name)))

  (def dependencies {:datasource {}
                     :other-component {}})

  (comment
    (cache/lookup-or-miss
      ;; cache atom
      fifo-cache
      ;; lookup key
      1004
      ;; fn that takes cache-key in case of miss, the result will be added to cache atom
      (partial pokemon-name-by-id-v2 dependencies))

    @fifo-cache)

  ;; This is a good read: https://dev.to/dpsutton/exploring-the-core-cache-api-57al
  ;; Pragmatic advice, always use wrapped API, and the only function you need is `lookup-or-miss`

  ;; In real service a good idea to put cache creation into a Component, so the cache could be passed as a normal dependency, plus we get reload in dev to clean up cache.

  (defrecord FiFoCacheComponent
    [config]
    component/Lifecycle
    (start [component]
      (println "Starting FiFoCacheComponent")
      (assoc component :fifo-cache
             (cache/fifo-cache-factory {} :threshold
                                       (:fifo-cache-threshold config))))
    (stop [component]
      (println "Stopping FiFoCacheComponent")
      (assoc component :fifo-cache nil)))


  ;; Note: all of those examples are in-memory cache, so be careful if you have multiple instances deployed
  ;; Take a look into a distributed cache instead (like Redis) if required.
  ;;
  ;;

  ;; #3 clojure.core.memoize if you need a different pattern

  (defn ^{:clojure.core.memoize/args-fn (fn [args]
                                          (println "args-fn:" args)
                                          (let [[_dependencies id] args]
                                            [id]))}
    pokemon-name-by-id-v3
    [dependencies id]
    (let [url (str "https://pokeapi.co/api/v2/pokemon-species/" id)]
      (println "Calling:" url)
      (-> (slurp url)
          (json/decode true)
          :name)))

  ;;Note: because you want memoization to read the metadata from your function, you must pass the Var in, rather than just the function name. #'pokemon-name-by-id-v3
  (def pokemon-name-by-id-v3-ttl
    (memoize/ttl
      #'pokemon-name-by-id-v3
      :ttl/threshold 10000))

  (comment
    (memoize/snapshot pokemon-name-by-id-v3-ttl)
    (pokemon-name-by-id-v3-ttl {:a 2} 403)
    (pokemon-name-by-id-v3-ttl {:a 1} 403)))








