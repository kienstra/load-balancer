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

(defn health-check []
  (dosync
   (alter be-apps (fn [previous-apps]
                    (reduce (fn [acc app]
                              (let [status (if (healthy? app) :healthy :unhealthy)]
                                (into acc {status (into (get acc status []) [app])})))
                            {}
                            (into (get previous-apps :healthy []) (get previous-apps :unhealthy [])))))))

(defn poll-health [interval]
  (let [c (chan)]
    (go
      (<! (timeout interval))
      (>! c (health-check)))
    (prn (<!! c))
    (close! c)
    (recur interval)))

(def app-sentinel (ref 0))
(defn increment-sentinel [apps]
  (dosync
   (alter app-sentinel (fn [previous]
                         (if
                          (= (count apps) (inc previous))
                           0
                           (inc previous))))))

(defn get-be-app []
  (let [app (nth (:healthy (deref be-apps)) (deref app-sentinel))]
    (increment-sentinel (:healthy (deref be-apps)))
    (app)))
