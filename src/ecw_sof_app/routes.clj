(ns ecw-sof-app.routes
  (:require
   [lambdaisland.uri :refer [uri]]
   [ecw-sof-app.config :refer [config]]))

(defn home [_req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "hi mom!"})

(defn launch [_req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "launch!"})

(def routes
  [["/" {:get home}]
   ["/launch" {:get launch}]])