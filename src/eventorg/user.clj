(ns eventorg.user
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]))
  (:require [eventorg.persist :as persist]))

(defn get-feed
  [user]
  )

(defn tags [abc]
  (response ["test1" "test2" "alarm" "mail" "urgent" "github" "etc"]))




(defroutes user-routes*
  (GET "/feed" request  (response
                         (-> (friend/identity request)
                            friend/current-authentication
                            :id
                            (#'persist/get-feed))))
  (POST "/feed" request  (-> (friend/identity request)
                            friend/current-authentication
                            :id
                            (#'persist/post-feed (-> request :params :tags flatten vec) (-> request :params :value))) "success")
  (GET "/tags" [request] (wrap-json-response tags)))

