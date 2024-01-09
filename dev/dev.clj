(ns dev
  (:require [com.stuartsierra.component.repl :as component-repl]
            [clojure-http-basics.core :as core]))

(component-repl/set-init
  (fn [_]
    (core/create-system
      {:server {:port 3001}})))