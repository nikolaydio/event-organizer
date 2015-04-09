(ns eventorg.core
  (:use [compojure.core :only (GET PUT POST defroutes context)])
  (:use [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.util.response :only [response]]
        [ring.middleware.session])
   (:use [ring.middleware params
      keyword-params
      nested-params
      multipart-params
      cookies
      session
      flash]))
  (:require (compojure handler route)
            [ring.util.response :as response]
            [eventorg.stream :as stream]
            [eventorg.user :as user])
  (:require [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))

(use 'org.httpkit.server)


(def home
  (response/resource-response "board.html"))


(def login-page
  (response/resource-response "welcome.html"))


(defroutes stream*
  (POST "/" [] "creating stream")
  (GET "/:id" { params :form-params } (wrap-json-response #'stream/api-streams-get))
  (POST "/:id" { params :form-params } (wrap-json-response #'stream/api-streams-post))
  (comment (GET "/:id/event/:seq" request (wrap-json-response #'stream/api-streams-get-event))))


(defroutes app-unsecure*
  (GET "/" request home)
  (GET "/login" request login-page)
  (GET "/authorized" request
       (friend/authorize #{::user} "This page can only be seen by authenticated users."))
  (friend/logout (POST "/logout" [] (ring.util.response/redirect "/")))
  (context "/api" []
    (context "/streams" [] #'stream*)
    (context "/user" [] (friend/wrap-authorize #'user/user-routes* #{::user}))
    )
  (compojure.route/resources "/")
  (compojure.route/not-found "Sorry, nothing here..."))

(def tusers {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "jane")
                    :roles #{::user}}})

(creds/bcrypt-credential-fn  tusers {:username "jane" :password "jane"})

(def app*
  (-> #'app-unsecure*
    (wrap-session)
    (wrap-keyword-params)
    (wrap-nested-params)
    (wrap-params)
    (friend/authenticate {:credential-fn (partial creds/bcrypt-credential-fn tusers)
                          :workflows [(workflows/interactive-form)]})

    ))
;;

(def app #'app)

(use '[ring.adapter.jetty :only (run-jetty)])
(def server (run-server #'app* {:port 8081 :join? false}))
