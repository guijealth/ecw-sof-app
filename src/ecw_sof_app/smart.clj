(ns ecw-sof-app.smart
  (:require
   [clojure.string :as str]
   [clj-http.client :refer [request]]
   [lambdaisland.uri :refer [map->query-string]]
   [ecw-sof-app.config :refer [config]])
  (:import [java.security MessageDigest SecureRandom]
           [java.util Base64]))

(def default-scopes
  ["openid"
   "profile"
   "fhirUser"
   "launch/patient"
   "offline_access"])

(def wk-smart-config
  (delay
    (:body (request {:method :get
                     :url (str (get-in @config [:ecw :iss]) "/.well-known/smart-configuration")
                     :as :json}))))

(defn gen-code-verifier []
  (let [bytes (byte-array 32)]
    (.nextBytes (SecureRandom.) bytes)
    (.encodeToString (Base64/getUrlEncoder) bytes)))

(defn code-challenge [code-verifier]
  (let [encoder (Base64/getUrlEncoder)
        sha-256 (MessageDigest/getInstance "SHA-256")]
    (as-> (.getBytes code-verifier "UTF-8") $
      (.digest sha-256 $)
      (.encodeToString encoder $)
      (str/replace $ #"=" ""))))

(defn standalone-launch [scopes]
  (let [code-verifier (gen-code-verifier)]
    (str (:authorization_endpoint @wk-smart-config)
         "?"
         (map->query-string {:response_type "code"
                             :client_id (get-in @config [:ecw :client :id])
                             :redirect_uri (get-in @config [:ecw :client :redirect-url])
                             :scope (->> scopes
                                         (concat default-scopes)
                                         (str/join " "))
                             :state (str (random-uuid))
                             :aud (get-in @config [:ecw :iss])
                             #_#_:code_verifier code-verifier
                             :code_challenge_method "S256"
                             :code_challenge (code-challenge code-verifier)}))))

(comment

  (select-keys @wk-smart-config [:authorization_endpoint :token_endpoint])

  (standalone-launch ["patient/Patient.read"])

  :.)