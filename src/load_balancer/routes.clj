(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]))

(defroutes
  app-routes
  (GET "/" request ((fn []
                      (let [headers (:headers request)]
                        (println "Handled request from" (get headers "host"))
                        (println (:request-method request) "/" (:scheme request))
                        (println "Host:" (:server-name request))
                        (println "User-Agent:" (get headers "user-agent"))
                        (println "Accept:" (get headers "accept"))

                        {:success 200}))))
    (route/not-found "Not Found"))

(defn app []
  (-> app-routes wrap-reload wrap-params wrap-session))
