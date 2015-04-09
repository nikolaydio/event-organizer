(ns eventorg.user
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response]))

(defn get-feed
  [request]
  (response (str request)))

(defn tags [abc]
  (response ["test1" "test2" "alarm" "mail" "urgent" "github" "etc"]))



(defroutes user-routes*
  (GET "/feed" [] (wrap-json-response #'get-feed))
  (GET "/tags" [] (wrap-json-response tags)))

