(ns load-balancer.round-robin
  (:require [clojure.core.async :refer [<! >! <!! chan close! go timeout]]
            [compojure.core :refer [defroutes GET]]
            [load-balancer.log :refer [log-request]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.mock.request :as mock]))

(defroutes
  be-app-routes
  (GET "/" request (do (log-request request)
                       (str "Hello from " (:server-port request)))))

(defn be-app []
  (-> be-app-routes wrap-reload wrap-params))

(defn get-be-apps [amount]
  (for [_ (range amount)]
    be-app))

(def be-apps (ref {:healthy (get-be-apps 10) :unhealthy []}))

(defn healthy? [app]
  (let [status (:status ((app) (mock/request :get "/")))]
    (and (>= status 200) (< status 300))))

(defn check-health []
  (dosync
   (alter be-apps (fn [previous-apps]
                    (reduce (fn [acc app]
                              (let [status (if (healthy? app) :healthy :unhealthy)]
                                (into acc {status (into (get acc status []) [app])})))
                            {}
                            (into (get previous-apps :healthy []) (get previous-apps :unhealthy [])))))))

(defn update-be-apps! [apps]
  (dosync
   (alter apps (fn [previous-apps]
                 (let [healthy (get previous-apps :healthy [])]
                   (into previous-apps {:healthy (conj (rest healthy) (first healthy))}))))))

(defn healthy-apps [apps]
  (:healthy (deref apps)))

(defn poll-health [time]
  (let [c (chan)]
    (go
      (<! (timeout time))
      (>! c (check-health)))
    (prn (<!! c))
    (close! c)
    (recur time)))

(defn get-be-app! []
  (let [app (first (healthy-apps be-apps))]
    (update-be-apps! be-apps)
    (app)))
