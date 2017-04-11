(ns ming.core
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(defn- wrap-handler
  [servers server-key handler]
  (fn [request]
    (swap! servers #(update-in % [server-key :requests] (fn [r] (conj r {:request request}))))
    (let [response (handler request)]
      response)))

(defn start
  [servers]
  (doseq [[server-key {:keys [handler port server]}] @servers]
    (if server
      (println (str "Server '" server-key "' already running on port " port))
      (do
        (println (str "Starting server '" server-key "' on port "  port))
        (swap! servers #(assoc-in % [server-key :server]
                                  (run-jetty (wrap-handler servers server-key handler) {:port port :join? false})))))))

(defn stop
  [servers]
  (doseq [[server-key {:keys [server port]}] @servers]
    (if server
      (do
        (println (str "Stopping server '" server-key "' on port " port))
        (.stop server)
        (swap! servers (fn [s] (update s server-key #(dissoc % :server)))))
      (println (str "Server '" server-key "' not running")))))

(defn requests
  [servers port]
  (-> @servers (get port) :requests))

(defmacro with-ming
  [server-spec & body]
  `(let [servers# (atom ~server-spec)]
     (try
       (start servers#)
       ~@body
       (finally
         (stop servers#)))))
