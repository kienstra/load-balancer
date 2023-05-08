(ns load-balancer.round-robin
  (:require [clojure.core.async :refer [<! >! <!! chan close! go timeout]]
            [org.httpkit.client :as client]
            [load-balancer.port :refer [port->url]]))

(def be-apps (ref {:healthy [] :unhealthy []}))

(defn healthy? [port]
  (let [status (get (deref (client/get (port->url port) {:headers {"accept" "*/*"}})) :status nil)]
    (and (>= status 200) (< status 300))))

(defn check-health []
  (dosync
   (alter be-apps (fn [previous-apps]
                    (reduce (fn [acc app]
                              (let [status (if (healthy? app) :healthy :unhealthy)]
                                (into acc {status (into (get acc status []) [app])})))
                            {}
                            (into (get previous-apps :healthy []) (get previous-apps :unhealthy [])))))))

(defn set-be-ports! [apps]
  (dosync
   (alter be-apps (fn [previous-apps]
                    (into previous-apps {:healthy apps})))))

(defn update-be-ports! [apps]
  (dosync
   (alter apps (fn [previous-apps]
                 (let [healthy (get previous-apps :healthy [])]
                   (into previous-apps {:healthy (concat (rest healthy) [(first healthy)])}))))))

(defn healthy-apps [apps]
  (:healthy (deref apps)))

(defn poll-health! [time]
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

(defn be-url! []
  (port->url (be-port!)))

(defn init! [ports polling-interval]
  (set-be-ports! ports)
  (poll-health! polling-interval))
