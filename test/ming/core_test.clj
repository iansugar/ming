(ns ming.core-test
  (:require [clojure.test :refer :all]
            [ming.core :as ming]
            [clj-http.client :as http]
            [picomock.core :as pico]
            [compojure.core :refer :all]
            [compojure.route :as route]))

(deftest make-a-request-minimal
  (ming/with-ming {:foo {:port 3101
                         :handler (fn [request] {:status 200
                                                :body "Hi!"})}}
    (let [response (http/get "http://localhost:3101/foo")]
      (is (= "Hi!"
             (-> response :body))))))

(deftest make-a-request-with-picomock-handler
  (let [handler (pico/mockval {:status 200 :body "hello"})]
    (ming/with-ming {:foo {:port 3101
                           :handler handler}}
      (testing "response as expected"
        (let [response (http/get "http://localhost:3101/foo")]
          (is (= "hello"
                 (-> response :body)))))
      (testing "request made just once and had correct path"
        (is (= 1
               (pico/mock-calls handler)))
        (is (= "/foo"
               (-> (pico/mock-args handler) first first :uri)))))))


(defroutes mockroutes
  (GET "/foo" [] "Hello!")
  (route/not-found "Not found"))

(deftest make-a-request-with-compojure-routes
  (ming/with-ming {:foo {:port 3101
                         :handler mockroutes}}
    (let [response (http/get "http://localhost:3101/foo")]
      (is (= "Hello!"
             (-> response :body)))
      (is (= 200
             (-> response :status))))
    (let [response (http/get "http://localhost:3101/bar"
                             {:throw-exceptions false})]
      (is (= 404
             (-> response :status))))))
