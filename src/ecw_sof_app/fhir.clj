(ns ecw-sof-app.fhir
  (:require
   [clojure.data.json :as json]
   [clj-http.client :refer [request]]))

(def ^:dynamic *base* nil)
(def ^:dynamic *auth* nil)

(defn search [rt criteria]
  (-> {:method :get
       :url (str *base* "/" rt)
       :query-params criteria
       :headers {"Authorization" *auth*
                 "Accept" "application/json"}}
      request
      :body
      (json/read-str :key-fn keyword)))
