(ns load-balancer.round-robin
  (:require [clojure.core.async :refer [<! >! <!! chan close! go timeout]]
            [clojure.string :refer [join]]
            [compojure.core :refer [GET]]
            [load-balancer.log :refer [log-request]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.mock.request :as mock]))

(defn get-route [server-number]
  (GET "/" request (do (log-request request)
                       {:success 200 :body (join " " ["Hello from server" server-number])})))

(defn get-app [server-number]
  (fn [] (-> (get-route server-number) wrap-reload wrap-params)))

(defn get-apps [amount]
  (map get-app (range amount)))

(def be-apps (ref {:healthy (get-apps 10) :unhealthy []}))

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

(defn first-healthy-app [apps]
  (first (:healthy (deref apps))))

(defn poll-health [time]
  (let [c (chan)]
    (go
      (<! (timeout time))
      (>! c (check-health)))
    (prn (<!! c))
    (close! c)
    (recur time)))

(defn get-be-app! []
  (let [app (first-healthy-app be-apps)]
    (update-be-apps! be-apps)
    (app)))
