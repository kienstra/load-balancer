(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.session :refer [wrap-session]]))

(defn log-request [request]
  (let [headers (:headers request)]
                        (println "Handled request from" (get headers "host"))
                        (println (:request-method request) "/" (:scheme request))
                        (println "Host:" (:server-name request))
                        (println "User-Agent:" (get headers "user-agent"))
                        (println "Accept:" (get headers "accept"))))

(defroutes
  be-app-routes
  (GET "/" request (do (log-request request)
                   {:success 200 :body "Replied with a hello message"})))

(defn be-app []
  (-> be-app-routes wrap-reload wrap-params wrap-session))

(defroutes
  lb-app-routes
  (GET "/" request (do (log-request request)
                       ((be-app) request))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params wrap-session))
