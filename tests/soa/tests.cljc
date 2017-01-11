(ns soa.tests
  (:require 
    [cljs.test :refer-macros [deftest is testing run-tests]]
    soa.core))

(def g (soa.core/graph [{:a 1}{:b 2}]))

(deftest sequenciality
  (is (= (count g) 2))
  (is (= (-> g first :a) 1))
  (is (= (map :a (into g g))
        '(1 nil 1 nil))))

(deftest associativity
  (is (= (keys g) 
        '(0 1)))
  ;possibly bad idea
  (is (= (contains? g :a)
         true))
  (is (= (keys (first g)) 
        '(:a :b)))
  (is (= (type (first g))
         soa.core.Node))
  ;novel keys
  (is (= (-> (soa.core/gassoc g 0 :z ::z) first keys)
        '(:a :b :z))))
 
(run-tests)