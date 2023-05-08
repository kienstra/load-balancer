(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [be-apps healthy-apps poll-health]]
            [load-balancer.routes :refer [be-app lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 80})
  (dorun (for [port (healthy-apps be-apps)]
           (run-server (be-app) {:port port})))
  (poll-health 10000))
