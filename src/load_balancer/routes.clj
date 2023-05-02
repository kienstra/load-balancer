(ns load-balancer.routes
  (:require [clojure.string :refer [join]]
            [compojure.core :refer [defroutes GET]]
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
    be-app-routes-1
    (GET "/" request (do (log-request request)
                         {:success 200 :body (join " " ["Hello from server" 1])})))

(defn be-app-1 []
  (-> be-app-routes-1 wrap-reload wrap-params wrap-session))

(defroutes
  be-app-routes-2
  (GET "/" request (do (log-request request)
                       {:success 200 :body (join " " ["Hello from server" 2])})))

(defn be-app-2 []
  (-> be-app-routes-2 wrap-reload wrap-params wrap-session))

(defn get-server []
  ((rand-nth [be-app-1 be-app-2])))

(defroutes
  lb-app-routes
  (GET "/" request (do (log-request request)
                       ((get-server) request))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params wrap-session))
