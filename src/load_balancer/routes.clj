(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [load-balancer.log :refer [log-request]]
            [org.httpkit.client :as client]))

(defn lb-app [url!]
  (-> (GET "/" request (do
                         (log-request request)
                         (:body (deref (client/request (into
                                                        (select-keys request [:method
                                                                              :timeout
                                                                              :connect-timeout
                                                                              :idle-timeout
                                                                              :query-params
                                                                              :as
                                                                              :form-params
                                                                              :client
                                                                              :body
                                                                              :basic-auth
                                                                              :user-agent])
                                                        {:url (url!) :headers {"accept" (get (:headers request) "accept")}}))))))
      wrap-reload
      wrap-params))

(defroutes
  be-app-routes
  (GET "/" request (do (log-request request)
                       (str "Hello from the web server running at " (:server-port request)))))

(defn be-app []
  (-> be-app-routes wrap-reload wrap-params))
