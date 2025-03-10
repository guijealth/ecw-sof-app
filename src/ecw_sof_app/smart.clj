(ns ecw-sof-app.smart
  (:require
   [clj-http.client :refer [request]]
   [ecw-sof-app.config :refer [config]]))

(defn wk-smart-config []
  (:body (request {:method :get
                   :url (str (get-in @config [:ecw :iss]) "/.well-known/smart-configuration")
                   :as :json})))

(comment

  (-> (wk-smart-config)
      (select-keys [:authorization_endpoint :token_endpoint]))

  :.)