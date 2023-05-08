(ns load-balancer.routes
  (:require [compojure.core :refer [defroutes GET]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [load-balancer.log :refer [log-request]]
            [org.httpkit.client :as client]
            [load-balancer.round-robin :refer [be-port!]]))

(defroutes
  lb-app-routes
  (GET "/" request (let [response (deref (client/request (into
                                                          (select-keys request [:method :timeout :connect-timeout :idle-timeout :query-params :as :form-params :client :body :basic-auth :user-agent])
                                                          {:url (str "http://localhost:" (be-port!))})))]
                     (log-request response)
                     (:body response))))

(defn lb-app []
  (-> lb-app-routes wrap-reload wrap-params))
