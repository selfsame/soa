(ns soa.core
  (:require [cljs.reader]))

(declare Node Graph)

(defprotocol IGraph
  (gget [o id][o id k]))

(defn- novel-key [col k]
  (if (contains? col k) col
      (assoc col k 
        (mapv (fn [_] nil) 
              (range (count (last (first col))))))))

(defn- extend-keys [col cnt]
  (reduce 
    (fn [r [k v]] 
      (assoc r k 
        (if (= (count v) cnt) v (conj v nil)))) 
    {} col))

(defn gupdate [o id k f & more]
  (Graph. (.-cnt o) 
    (update-in (novel-key (.-rec o) k) [k id] #(apply f % more))))

(defn gassoc [o id k v]
  (Graph. (.-cnt o) 
    (assoc-in (novel-key (.-rec o) k) [k id] v)))

(deftype Graph [cnt rec]
  IGraph
  (gget [o id] 
    (reduce 
      #(assoc %1 %2 (nth (get rec %2) id))
      rec (keys rec)))
  (gget [o id k] 
    (nth (get rec k) id))
  IWithMeta
  (-with-meta [o m] (Graph. cnt (-with-meta rec m)))
  IMeta
  (-meta [o] (-meta rec))
  ICounted
  (-count [coll] cnt)
  ISeqable
  (-seq [o] (map-indexed #(Node. o %1) (range cnt)))
  IIndexed
  (-nth [o n] (gget o n))
  (-nth [o n nf]
    (if (and (<= 0 n) (< n cnt))
        (gget o n) nf))
  ICollection
  (-conj [coll o]
    (assert (map? o) (str "soa.core/graph can't conj " o))
    (Graph. (inc cnt) 
      (extend-keys 
        (reduce 
          (fn [r [k v]] (update-in (novel-key r k) [k] conj v))
          rec o) (inc cnt))))
  IAssociative
  (-contains-key? [o k] (contains? rec k))
  ILookup
  (-lookup [this k] (-lookup rec k))
  (-lookup [this k nf] (-lookup rec k nf))
  IPrintWithWriter
  (-pr-writer [o writer opts] 
    (-write writer (str "#graph [" (apply str (map #(gget o %) (range cnt))) "]"))))

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

(defn graph 
  ([] (Graph. 0 {}))
  ([col] (into (graph) col)))