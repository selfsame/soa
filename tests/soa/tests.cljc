(ns soa.tests
  (:require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    soa.core))

(defrecord foo [a b c])
(def foo-graph (soa.core/graph 5 foo))

(deftest sequenciality
  (is (= (count foo-graph) 5))
  (is (= (-> foo-graph first :a) nil)))

(deftest associativity
  (is (= (keys foo-graph) 
        '(0 1 2 3 4)))
  ;possibly bad idea
  (is (= (contains? foo-graph :a)
         true))
  (is (= (keys (first foo-graph)) 
        '(:a :b :c)))
  (is (= (type (first foo-graph))
         soa.core.Node))
  (is (= (type (assoc (first foo-graph) ::z true))
         soa.tests/foo))
  (is (= (-> (soa.core/gupdate foo-graph 0 assoc :a ::z) first :a)
         ::z))
  ;gupdate ignores novel keys
  (is (= (-> (soa.core/gupdate foo-graph 0 assoc :z ::z) first keys)
        '(:a :b :c))))

(run-tests)