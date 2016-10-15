(ns agynamix.permissions-test
  (:require [clojure.test :refer :all]
            [agynamix.permissions :refer :all]))

(deftest create-permissions
  (testing "create wildcard permission"
    (is (= (make-permission "*") wildcard-permission)))

  (testing "create wildcard permission"
    (is (= (make-permission "company")
           (->Permission "company" #{wildcard-token} #{wildcard-token}))))

  (testing "create permission"
    (is (= (make-permission "company" "edit")
           (->Permission "company" #{"edit"} #{wildcard-token}))))

  (testing "create permission"
    (is (= (make-permission "company" "edit")
           (->Permission "company" #{"edit"} #{wildcard-token}))))

  (testing "create permission"
    (is (= (make-permission "company" "edit" "abcd1234")
           (->Permission "company" #{"edit"} #{"abcd1234"}))))

  (testing "create permission"
    (is (= (make-permission "company" "edit" "abcd1234")
           (->Permission "company" #{"edit"} #{"abcd1234"}))))

  )

(deftest simple-permissions-test

  (let [perm1 (make-permission "perm1")]
    (testing "test against wildcard permission"
      (is (implied-by? perm1 wildcard-permission)))

    (testing "test against empty permission"
      (is (not (implied-by? perm1 empty-permission))))

    (testing "test against perm1 domain"
      (is (implied-by? perm1 (make-permission "perm1"))))

    (testing "test against different domain"
      (is (not (implied-by? perm1 (make-permission "a")))))

    ))

(deftest domain-and-actions-test
  (testing "if company:* includes company:read"
    (let [user-perm (make-permission "company")
          res (make-permission "company" #{"read"})]
      (is (implied-by? res user-perm))))

  (testing "if company:read is included in wildcard permission"
    (let [res (make-permission "company" #{"read"})]
      (is (implied-by? res wildcard-permission))))

  (testing "if company:read is NOT included in empty permission"
    (let [res (make-permission "company" #{"read"})]
      (is (not (implied-by? res empty-permission)))))

  (testing "if company:* includes company:read"
    (let [user-perm (make-permission "company:*")
          res (make-permission "company:read")]
      (is (implied-by? res user-perm))))

  (testing "if company:* includes company:read"
    (let [user-perm (make-permission "company")
          res (make-permission "company:read")]
      (is (implied-by? res user-perm))))

  (testing "if company:read includes company:*"
    (let [user-perm (make-permission "company" #{"read"})
          res (make-permission "company")]
      (is (not (implied-by? res user-perm)))))

  (testing "if company:read includes company:*"
    (let [user-perm (make-permission "company:read")
          res (make-permission "company")]
      (is (not (implied-by? res user-perm)))))

  (testing "if multiple entities work. company:read,write includes company:upload,write"
    (let [user-perm (make-permission "company" #{"read" "write"})
          res (make-permission "company" #{"write" "upload"})]

      (is (implied-by? res user-perm))))

  (testing "if all wildcard works. * includes company:upload,write"
    (let [user-perm (make-permission)
          res (make-permission "company" #{"write" "upload"})]

      (is (implied-by? res user-perm))))
  )

(deftest actions-and-entities-test
  (let [edit (make-permission "users:edit:abcd1234")]

    (testing "checking actions and entities in wildcard permission"
      (is (implied-by? edit wildcard-permission)))

    (testing "checking actions and entities in empty permission"
      (is (not (implied-by? edit empty-permission))))

    (testing "checking actions and entities in empty permission"
      (is (not (implied-by? edit (make-permission "users" "create")))))

    (testing "checking actions and entities in empty permission"
      (is (not (implied-by? edit (make-permission "users" "update")))))

    (testing "checking actions and entities in empty permission"
      (is (not (implied-by? edit (make-permission "users" "view")))))

    (testing "checking actions and entities in empty permission"
      (is (implied-by? edit (make-permission "users" wildcard-token))))

    (testing "checking actions and entities in empty permission"
      (is (implied-by? edit (make-permission "users:*"))))

    (testing "checking actions and entities in empty permission"
      (is (implied-by? edit (make-permission "users:edit"))))

    (testing "checking actions and entities in empty permission"
      (is (not (implied-by? edit (make-permission "users:edit:abc")))))

    (testing "checking actions and entities in empty permission"
      (is (not (implied-by? edit (make-permission "users:edit:1234abc")))))

    (testing "checking actions and entities in empty permission"
      (is (implied-by? edit (make-permission "users:edit:abcd1234"))))

    )
  )

(deftest test-against-permission-list
  (let [perms [(make-permission "company:read")
               (make-permission "company:update")
               (make-permission "company:*")
               (make-permission "users:*")]]

    (testing
      (is (implied-by? (make-permission "company:write") perms)))

    (testing
      (is (not (implied-by? (make-permission "tickets:write") perms))))

    ))