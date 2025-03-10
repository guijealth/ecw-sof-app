(ns ecw-sof-app.chart
  (:require
   [clojure.string :as str]
   [ecw-sof-app.fhir :as fhir]))

(defn load-pat-chart [id]
  (letfn [(patient [bundle]
            (->> (:entry bundle)
                 (map :resource)
                 (filter (comp #{"Patient"} :resourceType))
                 (first)))
          (pract [ref bundle]
            (when ref
              (->> (:entry bundle)
                   (map :resource)
                   (filter (fn [{:keys [resourceType id]}]
                             (= ref (str resourceType "/" id))))
                   (first))))]
    ((fn [bundle]
       (let [pat (patient bundle)
             pract (-> pat :generalPractitioner :reference (pract bundle))]
         {:patient (select-keys pat [:name :gender :birthDate])
          :practitioner (select-keys pract [:name])}))
     (fhir/search "Patient"
                  {"_id" id
                   "_elements" (->> ["Patient.name"
                                     "Patient.gender"
                                     "Patient.birthDate"]
                                    (str/join ","))}))))