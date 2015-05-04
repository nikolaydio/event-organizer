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

(def db-name "eventorg")

(defn insert-msg [data id msg]
  (assoc data id (conj (get data id []) msg))
  )



(defn api-streams-post
  "Post a new event to a stream"
  [{req :params}]
  "(prn req)"
  (let [conn (bitemyapp.revise.connection/connect)]
    (if (get (first (get (-> (r/db db-name)
                             (r/table-db "streams")
                             (r/filter {:id (:id req) :write :true})
                             (r/pluck ["tags" "user"])
                             (r/merge {:time (r/now) :value (dissoc req :id)})
                             (r/foreach (r/lambda [record]
                                                  (-> (r/db db-name)
                                                      (r/table-db "events")
                                                      (r/insert record))))
                             (run conn)) :response nil )) :inserted nil)
      {:status 200  :headers {}   :body {:success :true}}
      {:status 401  :headers {}   :body {:success :false}})))



(defn api-streams-get
  "Get all data in a stream"
  [{req :params}]
  "(prn req)"
  (let [conn (bitemyapp.revise.connection/connect)]
    (let [resp (-> (r/db db-name)
                            (r/table-db "events")
                            (r/filter (r/lambda [event]
                                                (r/>= (-> (r/db db-name)
                                                    (r/table-db "streams")
                                                    (r/filter (r/lambda [stream]
                                                                        (r/and (r/= (r/get-field stream :user) (r/get-field event :user))
                                                                               (r/= (r/get-field stream :id) (:id req)))))
                                                    (r/count)) 1)))
                            (r/order-by (r/desc :time))
                            (run conn))]
      (prn resp)
      (response (first (:response resp))))))

