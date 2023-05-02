(ns load-balancer.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mock]
            [load-balancer.routes :refer [app]]))

(deftest test-app
  (testing "load balancing server"
    (let [response ((app) (mock/request :get "/example"))]
      (is (= (:status response) 200)))))
