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
(require '[org.httpkit.client :as http])

(def db-name "eventorg")



(defn get-user-tags [user-id]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "events")
        (r/filter {:user user-id} )
        (r/concat-map (r/fn [doc]
                     (r/get-field doc :tags)))
        (r/distinct)
        (r/run conn))))


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
  [user-id tags]
  (prn "Get feed request for user " user-id)
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "events")
        (r/filter {:user user-id})
        (r/filter
          (r/fn [row]
            (-> (r/set-intersection (r/get-field row :tags) tags)
                (r/eq tags))))
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
  (prn "Creating stream with id " user-id "and tags" tags)
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "streams")
        (r/insert {:user user-id
                   :tags tags} )
        (r/run conn))))

(defn list-streams
  "Return the all user streams by id"
  [user-id]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "streams")
        (r/filter {:user user-id} )
        (r/run conn))))

(defn delete-stream
  "Delete stream with certain id if user-id has access to it"
  [user-id stream-id]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "streams")
        (r/filter {:user user-id
                   :id stream-id} )
        (r/delete)
        (r/run conn))))


(defn stream-post
  "Post to a stream with id"
  [stream-id data]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "streams")
        (r/get stream-id)
        (r/pluck ["tags", "user"])
        (r/merge {:value data
                  :time (r/now)})
        (r/do (r/fn [doc] (-> (r/db db-name)
                              (r/table "events")
                              (r/insert doc))))
        (r/run conn))))

(defn create-hook
  "Create a new hook for a selected user"
  [user-id data]
  (prn "Creating hook for id " user-id "and data" data)
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "hooks")
        (r/insert (assoc data :user user-id) )
        (r/run conn))))

(defn list-hooks
  "Return the all user hooks by id"
  [user-id]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "hooks")
        (r/filter {:user user-id} )
        (r/run conn))))

(defn delete-hook
  "Delete hook with certain id if user-id has access to it"
  [user-id hook-id]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "hooks")
        (r/filter {:user user-id
                   :id hook-id} )
        (r/delete)
        (r/run conn))))

(defn user-from-stream
  [stream-id]
  (let [conn (connect :host "127.0.0.1" :port 28015)]
    (-> (r/db db-name)
        (r/table "streams")
        (r/filter {:id stream-id} )
        (r/pluck ["user"])
        (r/run conn))))


(require '[clojure.string :as str])

(defn strkey-to-keywords [strkey]
  (map keyword (str/split strkey #"\.")))

(defn deesc [string]
  (str/replace string #"\\\\" "\\"))

(re-pattern "\\w+")

(defn test-single-rule [rule data]
  (prn "Rule" rule)
  (let [fields (strkey-to-keywords (:field rule))]
    (prn "Field list" fields)
    (let [va (get-in data fields nil)]
      (prn va (:value rule))
      (when va
        (-> rule
            :value
            re-pattern
            (re-matches va)))
  )))

(defn test-rules [rules data]
  (every? #(#'test-single-rule (second (vec %1)) data) rules))

(defn get-replace-string [string data]
  (let [ keyword-list (-> (str/replace string #"%" "")
                           strkey-to-keywords) ]
    (get-in data keyword-list ":field not present:")))

(defn replace-content [content data]
  (let [special (re-seq #"%[\w\.]+%" content)]
      (reduce #(str/replace %1 %2 (get-replace-string %2 data)) content special)))


(defn dispatch-f [dispatch data]
  (prn "Dispatching " dispatch)
  (when dispatch
    (when (:url dispatch)
      (http/post (-> dispatch :url) {:body (replace-content (:content dispatch "") data)}))))

(defn run-request [stream-id data]
  (let [user-id (:user (first (user-from-stream stream-id)))
        hooks (list-hooks user-id)]
    (stream-post stream-id data)
    (prn hooks)
    (loop [v hooks]
      (when v
        (let [elem (first v)]
          (when elem
            (when (#'test-rules (:rules elem) data)
                (dispatch-f (:dispatch elem) data))
            (recur (next v))))
      ))))

(prn "abc")
(reduce #(do (prn "abc" %1 %2) (+ %1 %2)) [1 2 3 4])
(if nil "a" "b")
(first [])
(str/split "hellowod" #"\.")
