(ns eventorg.user
  (:use [compojure.core :only (GET PUT POST DELETE defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]))
  (:require [eventorg.persist :as persist]))


(defn tags []
  (response ["test1" "test2" "alarm" "mail" "urgent" "github" "etc"]))



(#'persist/list-streams "34e698ff-378c-4c5f-b1d5-e356d06c6292")

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
  (GET "/streams" request (-> (friend/identity request)
                            friend/current-authentication
                            :id
                            (#'persist/list-streams)
                            response))
  (POST "/streams" request (prn "Create stream request") (persist/create-stream (-> (friend/identity request)
                                                      friend/current-authentication
                                                      :id)
                                                  (-> request :params :tags flatten vec)))
  (DELETE "/streams/:id" request (-> (friend/identity request)
                                      friend/current-authentication
                                      :id
                                      (#'persist/delete-stream (-> request :params :id))))
  (GET "/hooks" request "ABC")
  (POST "/hooks" request "ABC")
  (DELETE "/hooks/:id" request "ABC")
  (GET "/tags" request  (tags)))

