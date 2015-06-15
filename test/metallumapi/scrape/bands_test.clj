(ns metallumapi.scrape.bands_test
  (:require [clojure.test :refer :all]
            [metallumapi.scrape.bands :refer :all]))

(deftest drop-colon-test
  (testing "Testing drop-colon"
    (is (= (drop-colon "test:")
           "test"))
    (is (= (drop-colon "A longer string:")
           "A longer string"))))

(deftest pretty-key-test
  (testing "Testing pretty-key"
    (is (= (pretty-key "A Pretty key:")
           :a-pretty-key))
    (is (= (pretty-key "Another key")
           :another-key))))

(deftest split-comma-test
  (testing "Testing split-comma"
    (is (= (split-comma " Some, Comma Delimited, Values   ")
           (list "Some" "Comma Delimited" "Values")))))

(deftest strip-whitespace-test
  (testing "Testing strip-whitespace"
    (is (= (strip-whitespace "\n\n\t\t\t\t\t\t       A \nString      \t\t\t\n")
           "A String"))))
