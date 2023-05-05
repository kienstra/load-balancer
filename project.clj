(defproject load-balancer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GPL-2.0-or-later"
            :url "https://www.gnu.org/licenses/old-licenses/gpl-2.0.txt"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.673"]
                 [compojure "1.7.0"]
                 [http-kit "2.6.0"]
                 [ring "1.10.0" :exclusions [ring/ring-core]]
                 [ring/ring-mock "0.3.2"]]
  :plugins [[lein-ring "0.12.6"]
            [lein-cljfmt "0.9.2"]]
  :ring {:handler load-balancer.handler/app}
  :main ^:skip-aot load-balancer.core
  :target-path "target/%s"
  :profiles {:dev
             [{:plugins [[com.jakemccrary/lein-test-refresh "0.23.0"]]}
              {:dependencies [[javax.servlet/servlet-api "2.5"]]}]
             :uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
