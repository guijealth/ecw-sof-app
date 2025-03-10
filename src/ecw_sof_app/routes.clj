(ns ecw-sof-app.routes
  (:require
   [taoensso.timbre :as log]
   [ecw-sof-app.smart :as smart]
   [ecw-sof-app.fhir :as fhir]
   [ecw-sof-app.chart :as chart]))

(defn ehr-launch [req]
  (let [launch (get-in req [:query-params "launch"])
        iss (get-in req [:query-params "iss"])]
    {:status 302
     :headers {"Location" (smart/ehr-launch launch iss ["patient/Patient.read"
                                                        "patient/Practitioner.read"])}}))

(defn standalone-launch [_req]
  {:status 302
   :headers {"Location" (smart/standalone-launch ["patient/Patient.read"
                                                  "patient/Practitioner.read"])}})

(defn callback [req]
  (let [code (get-in req [:query-params "code"])
        id (get-in req [:query-params "state"])
        {:keys [access_token token_type patient] :as resp} (smart/access-token id code)]
    (log/debug ::smart/access-token resp)
    (binding [fhir/*base* (smart/iss id)
              fhir/*auth* (str token_type " " access_token)]
      {:status 200
       :body (chart/load-pat-chart patient)})))

(def routes
  [["/ehr"
    ["/launch" {:get ehr-launch}]
    ["/callback" {:get callback}]]
   ["/standalone"
    ["/launch" {:get standalone-launch}]
    ["/callback" {:get callback}]]])