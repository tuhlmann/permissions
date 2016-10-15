(ns agynamix.roles-test
  (:require [clojure.test :refer :all]
            [agynamix.roles :refer :all]
            [agynamix.permissions :refer :all]))

(deftest init-roles-test
  (let [roles {
               :user/admin "user:*"
               :user/all   #{"user:read" "user:write"}
               :admin/all  "*"
               :company/super #{"company:read" "company:write" "company:edit" "company:delete"}
               }
        user {
              :roles       #{:user/all :company/super}
              :permissions #{"library:read" "company:gibberish"}
              }]
    (init-roles roles)

    (testing
      (is (has-permission? user "library:read")))

    (testing
      (is (has-permission? user "company:read")))

    (testing
      (is (has-permission? user "company:edit")))

    (testing
      (is (has-permission? user "company:gibberish")))

    (testing
      (is (has-permission? user #{"company:gibberish" "company:edit"})))

    (testing
      (is (lacks-permission? user #{"company:gibberish" "company:upload"})))

    (testing
      (is (lacks-permission? user "company:*")))

    (testing
      (is (lacks-permission? user (make-permission :user :*))))

    (testing
      (is (lacks-permission? user wildcard-permission)))

    (testing
      (is (lacks-permission? user "library:connect")))

    )
  )