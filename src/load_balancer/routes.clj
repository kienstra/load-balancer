(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [load-balancer.round-robin :refer [get-be-app!]]
            [load-balancer.log :refer [log-request]]))

(defroutes
  lb-app-routes
  (GET "/" request (do (log-request request)
                       ((get-be-app!) request))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params))
