(ns load-balancer.url)

(defn port->url [port]
  (str "http://localhost:" port))
