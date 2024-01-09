(ns clojure-http-basics.core
  (:require [com.stuartsierra.component :as component]
            [clojure-http-basics.components.http-server-component
             :as http-server-component]))


(defn create-system
  [config]
  (component/system-map
    :http-server-component (http-server-component/new-http-server-component config)
    ))

(defn -main
  []
  (let [system (-> {}
                   (create-system)
                   (component/start-system))]
    (.addShutdownHook
      (Runtime/getRuntime)
      (new Thread #(component/stop-system system)))))
