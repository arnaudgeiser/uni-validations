(ns uni-validations.core
  (:require [uni-validations.tree :refer [->tree]]))

(defn make-reconnu
  "Remove `:id` and `:parent` from the `etu-element`."
  [etu-element]
  (dissoc etu-element :id :parent))

(defn make-reconnaissance
  "Create a reconnaissance according to its type:
  - `RECONNAISSANCE` and `DISPENSE` are handled the same way.
  - `EQUIVALENCE` is supposed to replace another UE"
  [reconnaissance etu-elements-by-id]
  (let [reconnaissance' (dissoc reconnaissance
                                :etu-element-id
                                :etu-element-id-reconnu)]
    (condp = (:type reconnaissance)
      "RECONNAISSANCE" reconnaissance'
      "DISPENSE" reconnaissance'
      "EQUIVALENCE"
      (let [reconnu (:etu-element-id-reconnu reconnaissance)
            etu-element-reconnu (get etu-elements-by-id reconnu)]
        (assoc reconnaissance' :reconnu (make-reconnu etu-element-reconnu))))))

(defn make-etu-element
  "Remove `id`, `etu-element-id` and `parent` from the `etu-element`."
  [etu-element]
  (dissoc etu-element :id :etu-element-id :parent))

(defn make-resultats
  "Remove `etu-element-id` from the `resultats`."
  [resultats]
  (map #(dissoc % :etu-element-id) resultats))

(defn make-validation
  "Remove `etu-element-id` from the `validation`."
  [validation]
  (dissoc validation :etu-element-id))

(defn remplace?
  "Return whether an `etu-element-id` is `REMPLACE`."
  [validations etu-element-id]
  (let [validation (get validations etu-element-id)]
    (= (:etat-validation validation) "REMPLACE")))

(defprotocol MesValidationsRepository
  (find-etu-element [this])
  (find-resultats [this])
  (find-validations [this])
  (find-reconnaissances [this]))

(defn mes-validations
  "Find validations as a tree with the resultats or the reconnaissance."
  [mvr]
  (let [etu-elements (find-etu-element mvr)
        etu-elements-by-id (reduce #(assoc %1 (:id %2) %2) {} etu-elements)
        resultats (find-resultats mvr)
        validations (find-validations mvr)
        reconnaissances (find-reconnaissances mvr)]
    (->tree etu-elements
            {;; Un élément REMPLACE ne doit pas être affiché dans l'arbre
             ;; étudiant
             :ignore (fn [etu-element] (remplace? validations (:id etu-element)))
             :transform (fn [{:keys [id] :as etu-element}]
                          (let [resultats (get resultats id)
                                reconnaissance (get reconnaissances id)
                                validation (get validations id)]
                            (-> (make-etu-element etu-element)
                                (cond-> resultats (assoc :resultats (make-resultats resultats)))
                                (cond-> reconnaissance (assoc :reconnaissance (make-reconnaissance reconnaissance etu-elements-by-id)))
                                (cond-> validation (assoc :validation (make-validation validation))))))})))
