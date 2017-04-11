(ns ming.core-test
  (:require [clojure.test :refer :all]
            [ming.core :as ming]
            [clj-http.client :as http]
            [picomock.core :as pico]))

(defn foohandler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hi!"})

(deftest dosomemacro
  (testing "macro thing"
    (let [handler (pico/mockval {:status 200 :body "hello"})]
      (ming/with-ming {:foo {:port 3101
                             :handler handler}}
        (let [response (http/get "http://localhost:3101/foo")]
          (prn response)
          (is (= "hello"
                 (:body response)))
          (is (= 1
                 (pico/mock-calls handler))))))))
