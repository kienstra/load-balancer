(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.routes :refer [app]]))

(defn -main []
  (run-server (app) {:port 3000}))
