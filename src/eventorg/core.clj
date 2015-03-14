(ns eventorg.core
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]])
  (:require (compojure handler route)
            [ring.util.response :as response]
            [eventorg.stream :as stream]
            [ring.middleware.resource]))
(use 'org.httpkit.server)


(defn home [r]
  (ring.middleware.resource/wrap-resource #() "public/board.html"))



(defroutes stream*
  (POST "/" [] "creating stream")
  (GET "/:id" { params :form-params } (wrap-json-response #'stream/api-streams-get))
  (POST "/:id" { params :form-params } (wrap-json-response #'stream/api-streams-post))
  (comment (GET "/:id/event/:seq" request (wrap-json-response #'stream/api-streams-get-event))))
(defroutes users*
  (POST "/" { params :form-params } (wrap-json-response #({:key "creating user"})))
  (POST "/auth" { params :form-params } (wrap-json-response #({:key "authentication"})))
  (GET "/:id" [] "Getting streams"))

(defroutes app*
  (GET "/" [] home)
  (context "/api" []
    (context "/stream" [] #'stream*)
    (context "/user" [] #'users*))
  (compojure.route/resources "/")
  (compojure.route/not-found "Sorry, nothing here..."))

(def app (compojure.handler/api #'app*))

(use '[ring.adapter.jetty :only (run-jetty)])
(def server (run-server #'app {:port 8081 :join? false}))
