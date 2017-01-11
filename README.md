# soa

clojure [structure of arrays](https://en.wikipedia.org/wiki/AOS_and_SOA) data type

clojars
```clj
[selfsame/soa "0.5-SNAPSHOT"]
```

Soa graphs provide lower memory profile for collections of homogeneous data.

Briefly, instead of a vector of maps
```clj
[{:a 1, :b 2} 
 {:a 3, :b 4}]
```
they're a map of vectors.
```clj
{:a [1 3]
 :b [2 4]}
```


### usage

`soa.core/Graph` behaves like a vector, and maintains the internal SoA map.

```clj
(require 'soa.core)

(graph [{:a 1}{:b 2}])
;#graph [{:a 1, :b nil}{:a nil, :b 2}]

(.-rec g)
;{:a [1 nil], :b [nil 2]}

(into g g)
;#graph [{:a 1, :b nil}{:a nil, :b 2}{:a 1, :b nil}{:a nil, :b 2}]
```

The introduction of novel keys affects all items.

```clj
(conj g {:c 3})
;#graph [{:a 1, :b nil, :c nil}{:a nil, :b 2, :c nil}{:a nil, :b nil, :c 3}]
```

iterating graphs returns instances of `soa.core/Node`, a wrapper for the graph and index. Nodes behave like maps.

```clj
(first g)
#object[soa.core.Node]

(.-index (first g))
;0

(map :a g)
;(1 nil)

(soa.core/gget g 0)
;{:a 1, :b nil}
```

Use `gupdate` and `gassoc` to alter a graph.

```clj
(gupdate g 0 :b dec)
;#graph [{:a 1, :b -1}{:a nil, :b 2}]

(gassoc g 1 :z 3)
;#graph [{:a 1, :b nil, :z nil}{:a nil, :b 2, :z 3}]
```
