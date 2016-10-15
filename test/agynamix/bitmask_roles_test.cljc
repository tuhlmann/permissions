(ns agynamix.bitmask-roles-test
  (:require [clojure.test :refer :all]
            [agynamix.roles :refer :all]
            [agynamix.bitmask-roles :refer :all]
            [agynamix.permissions :refer :all]))

(deftest init-bitmask-roles-test
  (let [roles {
               2 "user:*"
               4   #{"user:read" "user:write"}
               8  "*"
               16 #{"company:read" "company:write" "company:edit" "company:delete"}
               32 "library:read"
               64 "company:read"
               128 "company:edit"
               256 "company:gibberish"
               512 "company:*"
               1024 "library:connect"
               }
        user {
              :roles 20
              :permissions #{"library:read" "company:gibberish"}
              }]
    (init-roles roles bitmask-permission-resolver bitmask-role-resolver)

    (testing
      (is (has-permission? user 32)))

    (testing
      (is (has-permission? user 64)))

    (testing
      (is (has-permission? user 128)))

    (testing
      (is (has-permission? user 256)))

    (testing
      (is (has-permission? user #{256, 128})))

    (testing
      (is (lacks-permission? user 512)))

    (testing
      (is (lacks-permission? user (make-permission :user :*))))

    (testing
      (is (lacks-permission? user wildcard-permission)))

    (testing
      (is (lacks-permission? user 1024)))

    )
  )