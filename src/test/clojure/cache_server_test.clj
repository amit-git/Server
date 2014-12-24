(ns cache-server-test
  (:require [clojure.test :refer :all]
    [cache-server.core :refer :all]))

(deftest cache-req-test
  (testing "request parse and cache load"
    (let [c (handle-cache-req "put color=blue,red")
          c1 (get-entry "color")
          c2 (handle-cache-req "get color")]
      (is (= "blue,red" (get c "color")))
      (is (= c2 c1))
      (is (= c1 (get c "color"))))))

(comment
  (run-tests)
  )

