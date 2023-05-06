(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [load-balancer.round-robin :refer [be-apps loop-health-check]]
            [load-balancer.routes :refer [lb-app]]))

(defn -main []
  (run-server (lb-app) {:port 3000})
  (let [apps (:healthy (deref be-apps))]
    (for [i (count apps)
          :let [app (i apps)]]
        (run-server (app) {:port (+ 8080 i)})))
  (loop-health-check))
