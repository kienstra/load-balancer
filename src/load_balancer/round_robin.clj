(ns load-balancer.round-robin
  (:require [compojure.core :refer [GET]]
            [clojure.string :refer [join]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [load-balancer.log :refer [log-request]]))


(defn get-route [server-number]
  (GET "/" request (do (log-request request)
                       {:success 200 :body (join " " ["Hello from server" server-number])})))

(defn get-app [server-number]
  (fn [] (-> (get-route server-number) wrap-reload wrap-params)))

(defn get-apps [amount]
  (map get-app (range amount)))

(def be-apps (ref {:healthy (get-apps 10)}))

(defn health-check []
  (dosync 
   (alter be-apps (fn [previous]
                    (map [])))))

(defn is-healthy [server]
  true)

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

; {:active [] :inactive []}
