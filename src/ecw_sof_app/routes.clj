(ns ecw-sof-app.routes
  (:require
   [ecw-sof-app.smart :as smart]))

(defn home [_req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "hi mom!"})

(defn launch [_req]
  {:status 302
   :headers {"Location" (smart/standalone-launch ["patient/Patient.read"])}})

(defn callback [_req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "callback!"})

(def routes
  [["/" {:get home}]
   ["/launch" {:get launch}]
   ["/callback" {:get callback}]])