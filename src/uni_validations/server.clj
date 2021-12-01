(ns uni-validations.server
  (:require [camel-snake-kebab.core :as csk]
            [ring.util.response     :refer [response]]
            [ring.middleware.json   :refer [wrap-json-response]]
            [ring.adapter.jetty     :as jetty]

            [uni-validations.core :refer [mes-validations]]))

(defn handler [mes-validations-repository _]
  (response (mes-validations mes-validations-repository)))

(defn run-http-server [handler]
  (jetty/run-jetty (wrap-json-response handler
                                       {:pretty true
                                        :key-fn csk/->camelCaseString}) ;; Wrap the response as JSON
                   {:port 9876
                    :join? false}))
