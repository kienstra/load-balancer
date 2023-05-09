(ns load-balancer.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mock]
            [load-balancer.routes :refer [lb-app]]))

(deftest test-app
  (testing "load balancing server"
    (let [response ((lb-app (fn [] "http://localhost:8080")) (mock/request :get "/"))]
      (is (= (:status response) 200)))))
