(ns uni-validations.core
  (:require [uni-validations.tree :refer [->tree]]))

(defn make-reconnu
  "Create an element `reconnu` by an EQUIVALENCE."
  [etu-element]
  (select-keys etu-element [:title]))

(defn make-reconnaissance
  "Create a reconnaissance according to its type:
  - `RECONNAISSANCE` and `DISPENSE` are handled the same way.
  - `EQUIVALENCE` is supposed to replace another UE contains in the `reconnu` key"
  [reconnaissance etu-elements-by-id]
  (let [reconnaissance' (select-keys reconnaissance
                                     [:grade
                                      :ects
                                      :etat-validation])]

    ;; Consider it as a switch over the reconnaissance's type
    (condp = (:type reconnaissance)
      "RECONNAISSANCE"
      reconnaissance'
      "DISPENSE"
      reconnaissance'
      "EQUIVALENCE"
      (let [reconnu (:etu-element-id-reconnu reconnaissance)
            etu-element-reconnu (get etu-elements-by-id reconnu)]
        (assoc reconnaissance' :reconnu (make-reconnu etu-element-reconnu))))))

(defn make-etu-element
  "Create an etu-element"
  [etu-element]
  (select-keys etu-element [:title]))

(defn make-resultats
  "Create resultats"
  [resultats]
  (map #(select-keys % [:tries :grades]) resultats))

(defn make-validation
  "Create a validation"
  [validation]
  (select-keys validation [:grade :ects :etat-validation]))

(defn remplace?
  "Return whether an `etu-element-id` is `REMPLACE`."
  [validations etu-element-id]
  (let [validation (get validations etu-element-id)
        etat-validation (:etat-validation validation)]
    (= etat-validation "REMPLACE")))

(defprotocol MesValidationsRepository
  "Protocol (interface) which defines
  methods that need to be implemented by the SQL store."
  (find-etu-elements [this])
  (find-resultats [this])
  (find-validations [this])
  (find-reconnaissances [this]))

(defn mes-validations
  "Find validations as a tree with the resultats or the reconnaissance."
  [repository]
  (let [etu-elements (find-etu-elements repository)
        etu-elements-by-id (reduce #(assoc %1 (:id %2) %2) {} etu-elements)
        resultats (find-resultats repository)
        validations (find-validations repository)
        reconnaissances (find-reconnaissances repository)]
    (->tree etu-elements
            {;; Un élément REMPLACE ne doit pas être affiché dans l'arbre
             ;; étudiant
             :ignore (fn [etu-element] (remplace? validations (:id etu-element)))
             :transform (fn [{:keys [id] :as etu-element}]
                          (let [resultats (get resultats id)
                                reconnaissance (get reconnaissances id)
                                validation (get validations id)]
                            (cond-> (make-etu-element etu-element)
                              resultats (assoc :resultats (make-resultats resultats))
                              reconnaissance (assoc :reconnaissance (make-reconnaissance reconnaissance etu-elements-by-id))
                              validation (assoc :validation (make-validation validation)))))})))
