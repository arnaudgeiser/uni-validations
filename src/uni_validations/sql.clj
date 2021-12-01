(ns uni-validations.sql
  (:require
   [clojure.java.io        :as io]
   [clojure.string         :as str]

   [next.jdbc.result-set   :as rs]
   [next.jdbc              :as jdbc]

   [uni-validations.core :refer [MesValidationsRepository]]))

(defn slurp-resource
  "Read a file from the classpath."
  [file]
  (slurp (io/resource file)))

(defn execute-query
  "Execute a SQL query on the database."
  [db-spec query]
  (jdbc/execute! db-spec [query] {:builder-fn rs/as-unqualified-kebab-maps}))

(defn load-db
  "Execute script `init.sql` on the database (create the tables and insert
  data)."
  [execute-query]
  (run! execute-query (clojure.string/split (slurp-resource "init.sql") #";")))

(def tree-query (slurp-resource "tree.sql"))
(def validations-query (slurp-resource "validations.sql"))
(def resultats-query (slurp-resource "resultats.sql"))
(def reconnaissances-query (slurp-resource "reconnaissances.sql"))

(defn find-etu-element
  "Find etu-elements."
  [execute-query]
  (execute-query tree-query))

(defn find-validations
  "Find validations and group them by `etu-element-id`."
  [execute-query]
  (reduce #(assoc %1 (:etu-element-id %2) %2) {}
          (execute-query validations-query)))

(defn find-resultats
  "Find the resultats and group them by `etu-element-id`."
  [execute-query]
  (group-by :etu-element-id (execute-query resultats-query)))

(defn find-reconnaissances
  "Find the reconnaissances and group them by `etu-element-id`."
  [execute-query]
  (reduce #(assoc %1 (:etu-element-id %2) %2) {}
          (execute-query reconnaissances-query)))

(defn make-mes-validations-repository [execute-query]
  (reify MesValidationsRepository
    (find-etu-element [_]
      (find-etu-element execute-query))
    (find-resultats [_]
      (find-resultats execute-query))
    (find-validations [_]
      (find-validations execute-query))
    (find-reconnaissances [_]
      (find-reconnaissances execute-query))))
