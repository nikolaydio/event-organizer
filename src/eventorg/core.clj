(ns eventorg.core
  (:use [compojure.core :only (GET PUT POST ANY defroutes context)])
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


(def board
  (response/resource-response "board.html"))


(def login-page
  (response/resource-response "welcome.html"))

(def home
  (response/resource-response "welcome.html"))


(defroutes stream*
  (POST "/" [] "creating stream")
  (GET "/:id" { params :form-params } (wrap-json-response #'stream/api-streams-get))
  (POST "/:id" { params :form-params } (wrap-json-response #'stream/api-streams-post))
  (comment (GET "/:id/event/:seq" request (wrap-json-response #'stream/api-streams-get-event))))

(def tusers {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "niki" {:id "e926cb1c-7d7e-4fbf-9433-09f3b00cf976"
                    :username "niki"
                    :password (creds/hash-bcrypt "niki")
                    :roles #{::user}}})

(derive ::admin ::user)

(defroutes app-unsecure*
  (GET "/" request (if (friend/identity request)
                                        board
                                        home))
  (GET "/login" request login-page)
  (friend/logout (ANY "/logout" [] (ring.util.response/redirect "/")))
  (context "/api" request
           (context "/user" request (-> #'user/user-routes*
                         (wrap-json-response)
                         (friend/wrap-authorize #{::user})))
    )
  (compojure.route/resources "/")
  (compojure.route/not-found "Sorry, nothing here..."))



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
