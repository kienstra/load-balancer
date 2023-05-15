(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [be-url! init!]]
            [load-balancer.routes :refer [be-app lb-app]]
            [load-balancer.port :refer [be-ports]]))

(defn -main []
  (run-server (lb-app be-url!) {:port 80})
  (let [ports (be-ports 10)]
    (dorun (for [port ports]
             (run-server (be-app) {:port port})))
    (init! ports 10000)))
