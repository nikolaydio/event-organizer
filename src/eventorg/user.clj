(ns eventorg.user
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(defn get-feed
  [user]
  (response (str user)))

(defn tags [abc]
  (response ["test1" "test2" "alarm" "mail" "urgent" "github" "etc"]))




(defroutes user-routes*
  (GET "/feed" [request] (response (friend/identity request) ))
  (GET "/tags" [request] (wrap-json-response tags)))

