(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [load-balancer.log :refer [log-request]]
            [org.httpkit.client :as client]
            [load-balancer.round-robin :refer [be-url!]]))

(defroutes
  lb-app-routes
  (GET "/" request (do
                     (log-request request)
                     (:body (deref (client/request (into
                                                    (select-keys request [:method :timeout :connect-timeout :idle-timeout :query-params :as :form-params :client :body :basic-auth :user-agent])
                                                    {:url (be-url!)})))))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params))

(defroutes
  be-app-routes
  (GET "/" request (do (log-request request)
                       (str "Hello from " (:server-port request)))))

(defn be-app []
  (-> be-app-routes wrap-reload wrap-params))
