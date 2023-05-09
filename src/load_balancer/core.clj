(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [init!]]
            [load-balancer.routes :refer [be-app lb-app]]
            [load-balancer.port :refer [be-ports]]))

(defn -main []
  (let [ports (be-ports 10)
        (run-server (lb-app (be-url!) {:port 80})
        url! (init! ports 10000)]
    (dorun (for [port ports]
             (run-server (url!) {:port port})))
    (init! ports 10000)))
