(ns uni-validations.tree)

(defn- root-node
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
