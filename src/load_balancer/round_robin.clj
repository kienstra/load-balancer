(ns load-balancer.round-robin
  (:require [clojure.core.async :refer [<! >! <!! chan close! go timeout]]
            [org.httpkit.client :as client]
            [load-balancer.port :refer [port->url]]))

(def be-ports (ref {:healthy [] :unhealthy []}))

(defn healthy? [port]
  (let [status (get (deref (client/get (port->url port) {:headers {"accept" "*/*"}})) :status nil)]
    (< 199 status 300)))

(defn check-health []
  (dosync
   (alter be-ports (fn [previous-ports]
                     (reduce (fn [acc port]
                               (let [status (if (healthy? port) :healthy :unhealthy)]
                                 (into acc {status (into (get acc status []) [port])})))
                             {}
                             (into (get previous-ports :healthy []) (get previous-ports :unhealthy [])))))))

(defn set-be-ports! [ports]
  (dosync
   (alter be-ports (fn [previous-ports]
                     (into previous-ports {:healthy (filter healthy? ports)})))))

(defn update-be-ports! [ports]
  (dosync
   (alter ports (fn [previous-ports]
                  (let [healthy (get previous-ports :healthy [])]
                    (into previous-ports {:healthy (concat (rest healthy) [(first healthy)])}))))))

(defn healthy-ports [ports]
  (:healthy (deref ports)))

(defn poll-health! [time]
  (let [c (chan)]
    (go
      (<! (timeout time))
      (>! c (check-health)))
    (println "Health results:" (<!! c))
    (close! c)
    (recur time)))

(defn be-port! []
  (let [port (first (healthy-ports be-ports))]
    (update-be-ports! be-ports)
    port))

(defn be-url! []
  (port->url (be-port!)))

(defn init! [ports polling-interval]
  (set-be-ports! ports)
  (poll-health! polling-interval))
