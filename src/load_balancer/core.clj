(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [be-apps loop-health-check]]
            [load-balancer.routes :refer [lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (map-indexed (fn [i server]
                 (run-server (server) {:port (+ 8080 i)}))
               be-apps)
  (loop-health-check))
