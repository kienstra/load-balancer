(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]))

(defroutes
  app-routes
  (GET "/" [] {:status 200})
  (route/not-found "Not Found"))

(defn app []
  (-> app-routes wrap-reload wrap-params wrap-session))
