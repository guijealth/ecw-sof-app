(ns ecw-sof-app.routes
  (:require
   [ecw-sof-app.smart :as smart]))

(defn ehr-launch [req]
  (let [launch (get-in req [:query-params "launch"])
        iss (get-in req [:query-params "iss"])]
    {:status 302
     :headers {"Location" (smart/ehr-launch launch iss ["patient/Patient.read"])}}))

(defn standalone-launch [_req]
  {:status 302
   :headers {"Location" (smart/standalone-launch ["patient/Patient.read"])}})

(defn callback [req]
  (let [code (get-in req [:query-params "code"])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     :body code}))

(def routes
  [["/ehr"
    ["/launch" {:get ehr-launch}]
    ["/callback" {:get callback}]]
   ["/standalone"
    ["/launch" {:get standalone-launch}]
    ["/callback" {:get callback}]]])