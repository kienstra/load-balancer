(ns load-balancer.log)

(defn log-request [request]
  (let [headers (:headers request)]
    (println "Handled request from" (get headers "host"))
    (println (:request-method request) "/" (:scheme request))
    (println "Port:" (:server-port request))
    (println "Host:" (:server-name request))
    (println "User-Agent:" (get headers "user-agent"))
    (println "Accept:" (get headers "accept"))))
