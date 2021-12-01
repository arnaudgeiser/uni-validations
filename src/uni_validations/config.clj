(ns uni-validations.config
  (:require [integrant.core :as ig]

            [uni-validations.sql    :as sql]
            [uni-validations.server :as server]))

(def db-spec
  "Database credentials."
  {:dbtype "postgresql"
   :dbname "postgres"
   :user "postgres"
   :password "pass"})

(def config
  {:sql/execute-query {:db-spec db-spec}
   :sql/load-db {:execute-query (ig/ref :sql/execute-query)}
   :core/mes-validations-repository {:execute-query (ig/ref :sql/execute-query)}
   :server/handler {:mes-validations-repository (ig/ref :core/mes-validations-repository)}
   :server/http {:handler (ig/ref :server/handler)}})

(defmethod ig/init-key :sql/execute-query [_ {:keys [db-spec]}]
  (partial sql/execute-query db-spec))

(defmethod ig/init-key :sql/load-db [_ {:keys [execute-query]}]
  (sql/load-db execute-query))

(defmethod ig/init-key :core/mes-validations-repository [_ {:keys [execute-query]}]
  (sql/make-mes-validations-repository execute-query))

(defmethod ig/init-key :server/handler [_ {:keys [mes-validations-repository]}]
  (partial server/handler mes-validations-repository))

(defmethod ig/init-key :server/http [_ {:keys [handler]}]
  (server/run-http-server handler))
