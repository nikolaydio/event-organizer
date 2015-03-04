(ns eventorg.stream
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response]))
(use 'org.httpkit.server)


(def subscribers (atom {}))

(require '[bitemyapp.revise.connection :only [connect close]])
(require '[bitemyapp.revise.query :as r])
(require '[bitemyapp.revise.core :refer [run run-async]])

(def local-conn (bitemyapp.revise.connection/connect))
(def db-name "eventorg")

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

  (-> (r/db db-name)
      (r/table-db "streams")
      (r/filter {:id "f3be18bf-408f-451f-8d10-e2fee82bb1a9" :write :true})
      (r/pluck ["tags" "user"])
      (r/merge {:time (r/now) :value "TestData"})
      (r/foreach (r/lambda [record]
                    (-> (r/db db-name)
                        (r/table-db "events")
                        (r/insert record))))
      (run local-conn))

(defn api-streams-post [{req :params}]
  (prn req)
  (if (get (first (get (-> (r/db db-name)
                  (r/table-db "streams")
                  (r/filter {:id (:id req) :write :true})
                  (r/pluck ["tags" "user"])
                  (r/merge {:time (r/now) :value (dissoc req :id)})
                  (r/foreach (r/lambda [record]
                                       (-> (r/db db-name)
                                           (r/table-db "events")
                                           (r/insert record))))
                  (run local-conn)) :response nil )) :inserted nil)
    {:status 200  :headers {}   :body {:success :true}}
    {:status 401  :headers {}   :body {:success :false}}))


(defn api-streams-get [{r :params}]
  (response (get @working-data (:id r) [])))



(defn api-streams-get-event [request]
  (let [r (:params request)]
    (if-let [result (get-in @working-data [(:id r) (read-string (:num r))])]
      result
      (with-channel request channel
            (swap! subscribers assoc [(:id r) (read-string (:num r))] channel))
    )))
