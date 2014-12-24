(ns cache-server-test
  (:require [clojure.test :refer :all]
    [cache-server.core :refer :all]))

; global state for tests
(def cache (atom {}))

(defn test-fixture
  [f]
  (reset! cache {})
  (f))

; fixture hooks
(use-fixtures :each test-fixture)

; tests
(deftest cache-req-test
  (testing "request parse and cache load"
    (let [c (handle-req cache "put color=blue,red")
          c1 (get-entry cache "color")
          c2 (handle-req cache "get color")]
      (is (= "blue,red" (get c "color")))
      (is (= c2 c1))
      (is (= c1 (get c "color"))))))

(deftest invalid-key-test
  (testing "get request with invalid key"
    (let [c (handle-req cache "get color")]
      (is (empty? c)))))

(deftest invalid-req-arg-1
  (testing "invalid request arguments"
    (let [c (handle-req cache "put color")]
      (is (nil? (:color c))))))

(deftest invalid-req-cmd
  (testing "invalid request command"
    (let [c (handle-req cache "color")]
      (is (empty? c)))))

(deftest empty-req
  (testing "empty request command"
    (let [c (handle-req cache "")]
      (is (empty? c)))))

(comment
  (run-tests)
  )

