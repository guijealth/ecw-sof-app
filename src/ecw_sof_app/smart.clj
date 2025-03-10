(ns ecw-sof-app.smart
  (:require
   [clojure.string :as str]
   [clj-http.client :refer [request]]
   [lambdaisland.uri :refer [map->query-string]]
   [ecw-sof-app.config :refer [config]])
  (:import [java.security MessageDigest SecureRandom]
           [java.util Base64]))

(def default-ehr-scopes
  ["openid"
   "profile"
   "fhirUser"
   "launch"])

(def default-standalone-scopes
  ["openid"
   "profile"
   "fhirUser"
   "launch/patient"
   "offline_access"])

(def state-storage (atom {}))

(defn wk-smart-config [iss]
  (-> {:method :get
       :url (str iss "/.well-known/smart-configuration")
       :as :json}
      request
      :body))

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

(defn ehr-launch [launch iss scopes]
  (letfn [(auth-challenge [params]
            (-> (wk-smart-config iss)
                :authorization_endpoint
                (str "?" (map->query-string params))))]
    (let [code-verifier (gen-code-verifier)
          state (str (random-uuid))]
      (swap! state-storage assoc state {:code-verifier code-verifier, :iss iss})
      (auth-challenge
       {:response_type "code"
        :client_id (get-in @config [:ecw :client :id])
        :redirect_uri (get-in @config [:ecw :client :redirect-url])
        :launch launch
        :scope (str/join " " (concat default-ehr-scopes scopes))
        :state state
        :aud iss
        :code_challenge_method "S256"
        :code_challenge (code-challenge code-verifier)}))))

(defn standalone-launch [scopes]
  (letfn [(auth-challenge [params]
            (-> (get-in @config [:ecw :iss])
                wk-smart-config
                :authorization_endpoint
                (str "?" (map->query-string params))))]
    (let [iss (get-in @config [:ecw :iss])
          code-verifier (gen-code-verifier)
          state (str (random-uuid))]
      (swap! state-storage assoc state {:code-verifier code-verifier, :iss iss})
      (auth-challenge
       {:response_type "code"
        :client_id (get-in @config [:ecw :client :id])
        :redirect_uri (get-in @config [:ecw :client :redirect-url])
        :scope (str/join " " (concat default-standalone-scopes scopes))
        :state state
        :aud iss
        #_#_:code_verifier code-verifier
        :code_challenge_method "S256"
        :code_challenge (code-challenge code-verifier)}))))

(defn iss [id]
  (get-in @state-storage [id :iss]))

(defn access-token [id code]
  (let [{:keys [code-verifier iss]} (get @state-storage id)]
    (-> {:method :post
         :url (:token_endpoint (wk-smart-config iss))
         :header {"Content-Type" "application/x-www-form-urlencoded"}
         :form-params {"grant_type" "authorization_code"
                       "code" code
                       "redirect_uri" (get-in @config [:ecw :client :redirect-url])
                       "code_verifier" code-verifier
                       "client_id" (get-in @config [:ecw :client :id])}
         :as :json}
        request
        :body)))

(comment

  (wk-smart-config (get-in @config [:ecw :iss]))

  (standalone-launch ["patient/Patient.read"])

  (ehr-launch "x72894HJS" (get-in @config [:ecw :iss]) ["patient/Patient.read"])

  :.)