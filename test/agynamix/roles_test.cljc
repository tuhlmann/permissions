(ns agynamix.roles-test
  (:require [clojure.test :refer :all]
            [agynamix.roles :refer :all]
            [agynamix.permissions :refer :all]))

(deftest init-roles-test
  (let [roles {
               "user/admin" "user:*"
               "user/all"   #{"user:read" "user:write"}
               "admin/all"  "*"
               "company/super" #{"company:read" "company:write" "company:edit" "company:delete"}
               "contacts/read" #{"contacts:read"}
               "timeline/edit" #{"timeline:edit" "timeline:read"}
               "project/all" #{"contacts/read" "timeline/edit" "project:read"}
               }
        user {
              :roles       #{"user/all" "company/super"}
              :permissions #{"library:read" "company:gibberish"}
              }
        membership {
                    :roles #{"project/all"}
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
      (is (not (has-permission? user #{"company:gibberish" "company:edit" "project:read"}))))

    (testing
      (is (has-any-permission? user #{"company:gibberish" "company:edit" "project:read"})))

    (testing
      (is (has-any-permission? membership #{"company:gibberish" "company:edit" "project:read"})))

    (testing
      (is (not (has-any-permission? membership #{"company:gibberish" "company:edit" "notExistent:read"}))))

    (testing
      (is (lacks-permission? user #{"company:gibberish" "company:upload"})))

    (testing
      (is (not (lacks-all-permissions? user #{"company:gibberish" "company:upload"}))))

    (testing
      (is (lacks-all-permissions? user #{"contacts/read" "company:upload"})))

    (testing
      (is (lacks-permission? user "company:*")))

    (testing
      (is (lacks-permission? user (make-permission :user :*))))

    (testing
      (is (lacks-permission? user wildcard-permission)))

    (testing
      (is (lacks-permission? user "library:connect")))

    (testing
      (is (has-permission? membership "timeline:read")))

    (testing
      (is (has-permission? membership "timeline:read")))

    (testing
      (is (has-permission? membership "contacts:read")))

    (testing
      (is (has-permission? membership "project:read")))

    )
  )