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
    (update-in (novel-key (.-soa o) k) [k id] #(apply f % more))))

(defn gassoc [o id k v]
  (Graph. (.-cnt o) 
    (assoc-in (novel-key (.-soa o) k) [k id] v)))

(deftype Graph [cnt soa]
  IGraph
  (gget [o id] 
    (reduce 
      #(assoc %1 %2 (nth (get soa %2) id))
      soa (keys soa)))
  (gget [o id k] 
    (nth (get soa k) id))
  IWithMeta
  (-with-meta [o m] (Graph. cnt (-with-meta soa m)))
  IMeta
  (-meta [o] (-meta soa))
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
    (assert (map? o) (str "soa.core/Graph can't conj " o))
    (Graph. (inc cnt) 
      (extend-keys 
        (reduce 
          (fn [r [k v]] (update-in (novel-key r k) [k] conj v))
          soa o) (inc cnt))))
  IAssociative
  (-contains-key? [o k] (contains? soa k))
  ILookup
  (-lookup [this k] (-lookup soa k))
  (-lookup [this k nf] (-lookup soa k nf))
  IPrintWithWriter
  (-pr-writer [o writer opts] 
    (-write writer (str "#soa/graph [" (apply str (map #(gget o %) (range cnt))) "]"))))

(deftype Node [graph index]
  IMap
  (-dissoc [o k] o)
  ILookup
  (-lookup [this k] (nth (get (.-soa graph) k) index))
  (-lookup [this k not-found])
  ISeqable
  (-seq [o] (-seq (gget graph index)))
  IMapEntry
  (-key [o] index)
  (-val [o] (gget graph index))
  IAssociative
  (-contains-key? [o k] (contains? (.-soa graph) k))
  (-assoc [o k v] (-assoc (gget graph index) k v))
  ICounted
  (-count [_] (count (.-soa graph)))
  IPrintWithWriter
  (-pr-writer [o writer opts] 
    (-write writer (str "#soa/node " (gget graph index)))))

(defn graph 
  ([] (Graph. 0 {}))
  ([col] (into (graph) col)))