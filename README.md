# soa

clojure [structure of arrays](https://en.wikipedia.org/wiki/AOS_and_SOA) data type

clojars
```clj
[selfsame/soa "0.0.1-SNAPSHOT"]
```

instead of a vector of records
```clj
[#user.z{:a 1, :b 2} 
 #user.z{:a 3, :b 4}]
```
a record of vectors 
```clj
[#user.z{:a [1 3]
	     :b [2 4]}]
```

### but why

lower memory profile

### usage

```clj
(require 'soa.core)

(defrecord foo [a b])

;make a graph of foo with 5 entries
(def foo-graph (soa.core/graph 5 foo))

(seq foo-graph)
;(#object[soa.core.Node] #object[soa.core.Node] ...)

(.-index (first foo-graph))
;0

(soa.core/gget foo-graph 0)
;#user.foo{:a nil, :b nil, :c nil}

(def foo-graph2 (soa.core/gupdate foo-graph 0 assoc :a 1))

(map :a foo-graph2)
;(1 nil nil ...)
```
