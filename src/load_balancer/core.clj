(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [be-app be-apps healthy-apps poll-health]]
            [load-balancer.routes :refer [lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (dorun (for [port (healthy-apps be-apps)]
           (run-server (be-app) {:port port})))
  (poll-health 10000))
