(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.routes :refer [be-app lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (run-server (be-app) {:port 4000}))
