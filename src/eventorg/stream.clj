(ns eventorg.stream
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response]))
(use 'org.httpkit.server)


(def working-data (atom {}))
(def subscribers (atom {}))



(defn insert-msg [data id msg]
  (assoc data id (conj (get data id []) msg))
  )


(defn notify [sid number msg]
  (prn @subscribers)
  (let [receiver (get @subscribers [sid number] nil)]
    (swap! subscribers #(dissoc %1 %2) [sid number])
    (if receiver
    (send! receiver (response {:msg msg}))
    nil)))


(defn api-streams-post [{r :params}]
  (let [result (swap! working-data insert-msg (:id r) (:msg r))]
    (notify (:id r) (dec (count (get result (:id r)))) (:msg r))
    (response {:key "success"})))

(defn api-streams-get [{r :params}]
  (response (get @working-data (:id r) [])))



(defn api-streams-get-event [request]
  (let [r (:params request)]
    (if-let [result (get-in @working-data [(:id r) (read-string (:num r))])]
      result
      (with-channel request channel
            (swap! subscribers assoc [(:id r) (read-string (:num r))] channel))
    )))
