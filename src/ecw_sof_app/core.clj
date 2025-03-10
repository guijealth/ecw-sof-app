(ns ecw-sof-app.core
  (:require
   [ring.adapter.jetty :refer [run-jetty]]
   [reitit.ring :as ring]
   [ring.logger.timbre :as ring-timbre]
   [reitit.coercion.spec :refer [coercion]]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.coercion :as rrc]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.dev.pretty :as pretty]
   [reitit.ring.middleware.exception :as exception]
   [muuntaja.core :as m]
   [taoensso.timbre :as log]
   [ecw-sof-app.config :refer [config]]
   [ecw-sof-app.routes :refer [routes]])
  (:gen-class))

(defonce server (atom nil))

(def app
  (ring/reloading-ring-handler
   #(ring/ring-handler
     (ring/router
      [routes]
      {:exception pretty/exception
       :data {:coercion coercion
              :muuntaja m/instance
              :middleware [exception/exception-middleware
                           parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           muuntaja/format-request-middleware
                           muuntaja/format-response-middleware
                           rrc/coerce-request-middleware
                           rrc/coerce-response-middleware
                           ring-timbre/wrap-with-logger]}})
     (ring/routes
      (ring/redirect-trailing-slash-handler)
      (ring/create-default-handler
       {:not-found (constantly {:status 404, :body "Not found"})})))))

(defn stop! []
  (log/info "stopping server...")
  (.stop @server))

(defn start! []
  (log/info "starting server...")
  (reset! server (run-jetty #'app {:port (:port @config), :join? false})))

(defn -main [& _]
  (start!))

(comment

  (start!)

  (stop!)

  :.)