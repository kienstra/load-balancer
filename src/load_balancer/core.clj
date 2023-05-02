(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.routes :refer [be-apps lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (map-indexed (fn [i server]
                 (run-server (server) {:port (+ 8080 i)}))
               be-apps))
