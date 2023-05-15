(ns load-balancer.core
  (:require [org.httpkit.server :refer [run-server]]
            [clojure.tools.cli :refer [parse-opts]]
            [load-balancer.round-robin :refer [be-url! init!]]
            [load-balancer.routes :refer [be-app lb-app]]
            [load-balancer.port :refer [be-ports]]))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 80
    :id :port
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 50000) "The port should be between 0 and 50000"]]
   ["-i" "--interval INTERVAL" "Polling interval"
    :default 10000
    :id :polling-interval
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 100000) "The interval should be between 0 and 100000"]]])

(defn -main [& args]
  (let [options (:options (parse-opts args cli-options))]
    (run-server (lb-app be-url!) {:port (:port options)})
    (let [ports (be-ports 10)]
      (dorun (for [port ports]
               (run-server (be-app) {:port port})))
      (init! ports (:polling-interval options)))))
