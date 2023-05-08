(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [be-apps healthy-apps poll-health]]
            [load-balancer.routes :refer [lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (let [apps (healthy-apps be-apps)]
    (dorun (for [i (range (count apps))
                 :let [app (nth apps i)]]
             (run-server (app) {:port (+ 8080 i)}))))
  (poll-health 10000))
