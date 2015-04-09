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
         flash])
  (:require (compojure handler route)
            [ring.util.response :as response]
            [compojure.handler :as handler]
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

(def tusers {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "jane")
                    :roles #{::user}}})

(derive ::admin ::user)

(defroutes app-unsecure*
  (GET "/" request home)
  (GET "/login" request login-page)
  (GET "/authorized" request
       (prn request)
       (response (friend/identity request)))
  (friend/logout (GET "/logout" [] (ring.util.response/redirect "/")))
  (context "/api" []
    (context "/streams" [] #'stream*)
    (context "/user" [] (friend/wrap-authorize #'user/user-routes* #{::user}))
    )
  (compojure.route/resources "/")
  (compojure.route/not-found "Sorry, nothing here..."))

(creds/bcrypt-credential-fn  tusers {:username "jane" :password "jane"})



(def app* (handler/site
           (friend/authenticate
             app-unsecure*
             {:allow-anon? true
             :login-uri "/login"
             :default-landing-uri "/"
             :unauthorized-handler #("Logged in?")
             :credential-fn #(creds/bcrypt-credential-fn tusers %)
             :workflows [(workflows/interactive-form)]})))

(use '[ring.adapter.jetty :only (run-jetty)])
(def server (run-server #'app* {:port 8081 :join? false}))
