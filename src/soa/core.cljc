(ns soa.core)

(declare Node)

(defprotocol IGraph
  (gget [o id][o id k]))

(deftype Graph [cnt rec]
  IGraph
  (gget [o id] 
    (reduce 
      #(assoc %1 %2 (nth (get rec %2) id))
      rec (keys rec)))
  (gget [o id k] 
    (nth (get rec k) id))
  ICounted
  (-count [coll] cnt)
  ISeqable
  (-seq [o] (map-indexed #(soa.core.Node. o %1) (range cnt)))
  IIndexed
  (-nth [o n] (gget o n))
  (-nth [o n nf]
    (if (and (<= 0 n) (< n cnt))
        (gget o n) nf))
  IAssociative
  (-contains-key? [o k] (contains? rec k))
  ILookup
  (-lookup [this k] (-lookup rec k))
  (-lookup [this k nf] (-lookup rec k nf)))

(deftype Node [graph index]
  IMap
  (-dissoc [o k] o)
  ILookup
  (-lookup [this k] (nth (get (.-rec graph) k) index))
  (-lookup [this k not-found])
  ISeqable
  (-seq [o] (-seq (gget graph index)))
  IMapEntry
  (-key [o] index)
  (-val [o] (gget graph index))
  IAssociative
  (-contains-key? [o k] (contains? (.-rec graph) k))
  (-assoc [o k v] (-assoc (gget graph index) k v))
  ICounted
  (-count [_] (count (.-rec graph))))

(defn gupdate [o id f & more]
  (let [node (apply f (nth o id) more)]
    (Graph. (.-cnt o)
      (reduce
        (fn [r [k v]]
          (if (contains? r k)
              (assoc-in r [k id] v) r))
        (.-rec o) node))))

(defn graph [cnt rec-type]
  (let [rec (new rec-type)]
    (Graph. cnt 
      (reduce 
        #(assoc %1 %2 (into [] (take cnt (repeat nil))))
        rec (keys rec)))))