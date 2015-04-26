(ns eventorg.persist
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response]))


;(require '[bitemyapp.revise.connection :only [connect close]])
;(require '[bitemyapp.revise.query :as r])
;(require '[bitemyapp.revise.core :refer [run run-async]])

(require '[rethinkdb.core :refer [connect close]])
(require '[rethinkdb.query :as r])

(def db-name "eventorg")



(defn check-user [user pass]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (let [data (-> (r/db db-name)
                  (r/table "users")
                  (r/filter {:username user :password pass})
                  (r/run conn))]
          (if (empty? data)
            nil
            (first data)))))

(defn create-user [user pass]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (->  (r/branch  (-> (r/db db-name)
                       (r/table "users")
                       (r/filter {:username user})
                       (r/count)
                       (r/eq 0))
                  (-> (r/db db-name)
                      (r/table "users")
                      (r/insert {:username user
                                 :password pass} ))
                   {:errors 1})
        (r/run conn))))


(comment
  (defn get-feed
  "get the feed for a selected user"
  [user-id]
  (prn "Get feed request for user " user-id)
  (let [conn (bitemyapp.revise.connection/connect)]
    (-> (r/db db-name)
        (r/table-db "events")
        (r/filter {:user user-id})
        (r/without :user)
        (r/order-by (r/desc "time"))
        (r/limit 20)
        (run conn)
        :response
        first)))
  )

(defn get-feed
  "get the feed for a selected user"
  [user-id]
  (prn "Get feed request for user " user-id)
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "events")
        (r/filter {:user user-id})
        (r/order-by (r/desc "time"))
        (r/without [:user :time])
        (r/limit 20)
        (r/run conn))))


(comment (defn post-feed
  "post a new event to a selected user's feed. Does not verify user id"
  [user-id tags value]
  (prn "Posting event for user " user-id tags value)
  (let [conn (bitemyapp.revise.connection/connect)]
    (-> (r/db db-name)
        (r/table-db "events")
        (r/insert {:user user-id
                   :time (r/now)
                   :tags tags
                   :value value} )
        (run conn)))))

(defn post-feed
  "post a new event to a selected user's feed. Does not verify user id"
  [user-id tags value]
  (prn "Posting event for user " user-id tags value)
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "events")
        (r/insert {:user user-id
                   :time (r/now)
                   :tags tags
                   :value value} )
        (r/run conn))))


(defn create-stream
  "Create a new stream for a selected user"
  [user-id tags]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "streams")
        (r/insert {:user user-id
                   :tags tags
                   :subpubhubbub []
                   :url ""} )
        (r/run conn))))
