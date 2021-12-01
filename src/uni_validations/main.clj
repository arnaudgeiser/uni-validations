(ns uni-validations.main
  (:require [integrant.core :as ig]

            [uni-validations.config :as config])
  (:gen-class))

(defn -main [& _]
  (ig/init config/config))
