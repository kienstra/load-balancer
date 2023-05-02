(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.routes :refer [be-app-1 be-app-2 lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (run-server (be-app-1) {:port 8081})
  (run-server (be-app-2) {:port 8082}))
