(ns load-balancer.routes
  (:require [clojure.string :refer [join]]
            [compojure.core :refer [defroutes GET]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]))

(defn log-request [request]
  (let [headers (:headers request)]
                        (println "Handled request from" (get headers "host"))
                        (println (:request-method request) "/" (:scheme request))
                        (println "Host:" (:server-name request))
                        (println "User-Agent:" (get headers "user-agent"))
                        (println "Accept:" (get headers "accept"))))

(defn get-route [server-number]
  (GET "/" request (do (log-request request)
                       {:success 200 :body (join " " ["Hello from server" server-number])})))

(defn get-app [server-number] 
  (fn [] (-> (get-route server-number) wrap-reload wrap-params)))

(defn get-apps [amount]
  (map get-app (range amount)))

(def be-apps (get-apps 10))

(defn get-be-app []
  ((rand-nth be-apps)))

(defroutes
  lb-app-routes
  (GET "/" request (do (log-request request)
                       ((get-be-app) request))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params))
