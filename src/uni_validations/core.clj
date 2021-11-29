(ns uni-validations.core
  (:require
   [clojure.java.io        :as io]
   [clojure.string         :as str]

   [camel-snake-kebab.core :as csk]
   [next.jdbc.result-set   :as rs]
   [next.jdbc              :as jdbc]
   [ring.util.response     :refer [response]]
   [ring.middleware.json   :refer [wrap-json-response]]
   [ring.adapter.jetty     :as jetty])
  (:gen-class))

(def db-spec
  "Database credentials."
  {:dbtype "postgresql"
   :dbname "postgres"
   :user "postgres"
   :password "pass"})

(defn slurp-resource
  "Read a file from the classpath."
  [file]
  (slurp (io/resource file)))

(defn execute-query
  "Execute a SQL query on the database."
  [query]
  (jdbc/execute! db-spec [query] {:builder-fn rs/as-unqualified-kebab-maps}))

(defn load-db
  "Execute script `init.sql` on the database (create the tables and insert
  data)."
  []
  (run! execute-query (clojure.string/split (slurp-resource "init.sql") #";")))

(def tree-query (slurp-resource "tree.sql"))
(def validations-query (slurp-resource "validations.sql"))
(def resultats-query (slurp-resource "resultats.sql"))
(def reconnaissances-query (slurp-resource "reconnaissances.sql"))

(defn find-etu-element
  "Find etu-elements."
  []
  (execute-query tree-query))

(defn find-validations
  "Find validations and group them by `etu-element-id`."
  []
  (reduce #(assoc %1 (:etu-element-id %2) %2) {}
          (execute-query validations-query)))

(defn find-resultats
  "Find the resultats and group them by `etu-element-id`."
  []
  (group-by :etu-element-id (execute-query resultats-query)))

(defn find-reconnaissances
  "Find the reconnaissances and group them by `etu-element-id`."
  []
  (reduce #(assoc %1 (:etu-element-id %2) %2) {}
          (execute-query reconnaissances-query)))

(defn root-node
  "Find the root node from a `coll`.
  The root node is the one which doesn't have a parent."
  [coll]
  (first (filter #(nil? (:parent %)) coll)))

(defn ->tree
  "Convert a `coll` into a `tree` and optionally execute a `transform` function
  over each element.
  Some elements won't appear if they are ignore by the `ignore` function.
  Each child is stored in the `children` key."
  ([coll opts] (->tree coll (root-node coll) opts))
  ([coll node {:keys [transform ignore]
               :or {transform identity
                    ignore (constantly false)} :as opts}]
   (let [children (filter #(= (:parent %) (:id node)) coll)]
     (if (seq children)
       (assoc (transform node) :children (->> children
                                              (remove ignore)
                                              (map #(->tree coll % opts))))
       (transform node)))))

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

(defn mes-validations
  "Find validations as a tree with the resultats or the reconnaissance."
  []
  (let [etu-elements (find-etu-element)
        etu-elements-by-id (reduce #(assoc %1 (:id %2) %2) {} etu-elements)
        resultats (find-resultats)
        validations (find-validations)
        reconnaissances (find-reconnaissances)]
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

(defn handler [_] (response (mes-validations)))

(defn run-http-server []
  (jetty/run-jetty (wrap-json-response #'handler
                                       {:pretty true
                                        :key-fn csk/->camelCaseString}) ;; Wrap the response as JSON
                   {:port 9876
                    :join? false}))

(defn -main [& _]
  ;; Load the database
  (load-db)
  ;; Run the HTTP server on port 9876
  (run-http-server))
