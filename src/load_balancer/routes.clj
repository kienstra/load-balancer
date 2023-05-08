(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [load-balancer.log :refer [log-request]]
            [org.httpkit.client :as client]))

(defroutes
  lb-app-routes
  (GET "/" request (let [response (deref (client/request (into
                                                          (select-keys request [:method :timeout :connect-timeout :idle-timeout :query-params :as :form-params :client :body :basic-auth :user-agent])
                                                          {:url "http://localhost:8080"})))]
                     (log-request response)
                     (:body response))))
;;                                             (select-keys request [:method :headers :timeout :connect-timeout :idle-timeout :query-params :as :form-params :client :body :basic-auth :user-agent]))))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params))
