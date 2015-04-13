(ns eventorg.persist
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response]))


(require '[bitemyapp.revise.connection :only [connect close]])
(require '[bitemyapp.revise.query :as r])
(require '[bitemyapp.revise.core :refer [run run-async]])

(def db-name "eventorg")


(defn get-feed
  "get the feed for a selected user"
  [user-id]
  (prn "Get feed request for user " user-id)
  (let [conn (bitemyapp.revise.connection/connect)]
    (-> (r/db db-name)
        (r/table-db "events")
        (r/filter {:user user-id})
        (r/limit 20)
        (run conn)
        :response)))
