(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [poll-health set-be-ports]]
            [load-balancer.routes :refer [be-app lb-app]]
            [load-balancer.port :refer [be-ports]]))

(defn -main []
  (run-server (lb-app) {:port 80})
  (let [ports (be-ports 10)]
    (set-be-ports ports)
    (dorun (for [port ports]
             (run-server (be-app) {:port port}))))
  (poll-health 10000))
