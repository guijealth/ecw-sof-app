(ns ecw-sof-app.config
  (:require
   [config.dotenv :as dotenv]
   [config.sysenv :as sysenv]
   [config.core :as cfg]))

(def struc
  [[:port {:env "PORT", :of-type :cfg/integer}]
   [:ecw
    [:iss {:env "ECW_ISS", :of-type :cfg/url}]
    [:client
     [:id {:env "ECW_CLIENT_ID", :of-type :cfg/string}]
     [:secret {:env "ECW_CLIENT_SECRET", :of-type :cfg/string}]
     [:redirect-url {:env "ECW_REDIRECT_URL", :of-type :cfg/url}]]]
   [:auth {:env "AUTH", :of-type :cfg/string}]])

(def config
  (delay
    (-> {}
        (cfg/patch (dotenv/parse ".env") struc)
        (cfg/patch (sysenv/read-all) struc))))