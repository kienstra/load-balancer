(ns load-balancer.port)

(defn port->url [port]
  (str "http://localhost:" port))

(defn be-ports [amount]
  (map #(+ 8080 %) (range amount)))
