(ns clojure-http-basics.components.http-server-component
  (:require [clojure.edn :as edn]
            [com.stuartsierra.component :as component]
            [crypto.random :as random]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [compojure.core :refer [GET routes]]
            [compojure.route :as route]
            [ring.middleware.session.memory :as memory-store]
            [ring.middleware.session.cookie :as cookie-store]
            [ring.util.response :as response]
            [ring.middleware.session :as ring-session]
            [clojure.pprint])
  (:import (org.eclipse.jetty.server Server)))

(defn my-new-middleware
  [handler]
  (fn [request]
    (clojure.pprint/pprint request)
    (let [response (handler request)]
      (println response)
      response)))


;; With Compojure
(defn compojure-root-handler
  [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (str "Compojure Root")})

(def user-info
  {:name "Andrey Fadeev"
   :email "some@email.com"
   :id (random-uuid)})

(def compojure-routes
  (routes
    (GET "/login" request
      (let [credentials (-> request
                            :params
                            (select-keys [:username
                                          :password]))]
        (if (= credentials
               {:username "test"
                :password "test"})
          {:status 200
           :body "I'm inside"
           :session user-info}
          {:status 200
           :body "credentials are not correct"})
        )
      )
    (GET "/info" request
      (let [{:keys [session]} request]
        (if (seq session)
          {:status 200
           :body (str "HI: " session)}
          {:status 200
           :body "NO SESSION"})))
    (GET "/logout" request
      {:status 200
       :session nil}
      )




    (route/not-found "Page not found")))

; cookie-attrs
;:domain - restrict the cookie to a specific domain
;:path - restrict the cookie to a specific path
;:secure - restrict the cookie to HTTPS URLs if true
;:http-only - restrict the cookie to HTTP if true (not accessible via e.g. JavaScript)
;:max-age - the number of seconds until the cookie expires
;:expires - a specific date and time the cookie expires

(def custom-memory-store-atom (atom {}))

(defrecord HttpServerComponent
  [config]
  component/Lifecycle

  (start [component]
    (println "Starting HttpServerComponent")
    (let [server (jetty/run-jetty
                   (-> compojure-routes
                       (my-new-middleware)
                       (keyword-params/wrap-keyword-params)
                       (params/wrap-params)
                       (ring-session/wrap-session
                         {:cookie-attrs {:secure true
                                         :max-age (* 60 10)}
                          :cookie-name "my-clojure-app-session-cookie"
                          :store (cookie-store/cookie-store
                                   {:key (-> (slurp "config.edn")
                                             (edn/read-string)
                                             :cookie-store-key
                                             (byte-array))})
                          }))
                   {:port 3000
                    :join? false})]
      (assoc component :server server)))

  (stop [component]
    (println "Stopping HttpServerComponent")
    (when-let [^Server server (:server component)]
      (.stop server))
    (assoc component :server nil)))

(defn new-http-server-component
  [config]
  (map->HttpServerComponent {:config config}))

















(comment
  (vec (random/bytes 16))
  (byte-array (:key (edn/read-string (slurp "config.edn"))))
  (ring-session/wrap-session {:cookie-name "my-app-session-name"
                              :cookie-attrs {:http-only true
                                             :secure true
                                             :max-age (* 60 10)}
                              ;:store (memory-store/memory-store custom-session-atom)
                              :store (cookie-store/cookie-store {:key (byte-array (:key (edn/read-string (slurp "config.edn"))))})
                              }))