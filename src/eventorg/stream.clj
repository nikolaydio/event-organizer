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

(defn api-streams-post
  "Post a new event to a stream"
  [{req :params}]
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




(defn api-streams-get
  "Get all data in a stream"
  [{req :params}]
  (prn req)
  (response (:response (-> (r/db db-name)
                          (r/table-db "events")
                          (r/filter (r/lambda [event]
                                              (r/>= (-> (r/db db-name)
                                                  (r/table-db "streams")
                                                  (r/filter (r/lambda [stream]
                                                                      (r/and (r/= (r/get-field stream :user) (r/get-field event :user))
                                                                             (r/= (r/get-field stream :id) (:id req)))))
                                                  (r/count)) 1)))
                          (run local-conn)))))



(defn api-streams-get-event [request]
  (let [r (:params request)]
    (if-let [result (get-in @working-data [(:id r) (read-string (:num r))])]
      result
      (with-channel request channel
            (swap! subscribers assoc [(:id r) (read-string (:num r))] channel))
    )))
