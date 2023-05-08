(ns load-balancer.round-robin
  (:require [clojure.core.async :refer [<! >! <!! chan close! go timeout]]
            [compojure.core :refer [defroutes GET]]
            [load-balancer.log :refer [log-request]]
            [org.httpkit.client :as client]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]))

(defroutes
  be-app-routes
  (GET "/" request (do (log-request request)
                       (str "Hello from " (:server-port request)))))

(defn be-app []
  (-> be-app-routes wrap-reload wrap-params))

(defn be-ports [amount]
  (map #(+ 8080 %) (range amount)))

(def be-apps (ref {:healthy (be-ports 10) :unhealthy []}))

(defn healthy? [port]
  (let [status (:status (deref (client/get (str "http://localhost:" port))))]
    (and (>= status 200) (< status 300))))

(defn check-health []
  (dosync
   (alter be-apps (fn [previous-apps]
                    (reduce (fn [acc app]
                              (let [status (if (healthy? app) :healthy :unhealthy)]
                                (into acc {status (into (get acc status []) [app])})))
                            {}
                            (into (get previous-apps :healthy []) (get previous-apps :unhealthy [])))))))

(defn update-be-ports! [apps]
  (dosync
   (alter apps (fn [previous-apps]
                 (let [healthy (get previous-apps :healthy [])]
                   (into previous-apps {:healthy (concat (rest healthy) [(first healthy)])}))))))

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

(defn be-port! []
  (let [app (first (healthy-apps be-apps))]
    (update-be-ports! be-apps)
    app))
