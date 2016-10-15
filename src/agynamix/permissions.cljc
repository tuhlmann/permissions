(ns agynamix.permissions
  (:require [clojure.string :as str]))

(def wildcard-token :*)
(def empty-permission-token :none)
(def part-divider ":")
(def subpart-divider ",")

(defrecord Permission [domain   ;; a keyword
                       actions   ;; a set of keywords
                       entities] ;; a set of keywords
  Object
  (toString [_] (str (name domain) ":" (str/join "," (map name actions)) ":" (str/join "," (map name entities)))))

(def wildcard-permission (->Permission wildcard-token #{wildcard-token} #{wildcard-token}))
(def empty-permission (->Permission empty-permission-token #{} #{}))

(defn- make-subpart-set [str-or-set]

  (cond
    (set? str-or-set) str-or-set

    (keyword? str-or-set) #{str-or-set}

    (string? str-or-set)
    (into #{} (map keyword (str/split str-or-set (re-pattern subpart-divider))))

    :else #{}))

(defn make-permission
  ([] (make-permission wildcard-token))
  ([domain]
   (cond
     (instance? Permission domain) domain

     (and
       (string? domain)
       (str/index-of domain part-divider))
     (apply make-permission (str/split domain (re-pattern part-divider)))

     :else
     (make-permission domain #{wildcard-token} #{wildcard-token})))

  ([domain actions]
   (make-permission domain (make-subpart-set actions) #{wildcard-token}))

  ([domain actions entities]
   (->Permission (keyword domain) (make-subpart-set actions) (make-subpart-set entities))))

(defn- actions-and-entities-implied-by? [resource-perm user-perm]
  (let [resource-actions (:actions resource-perm)
        user-actions (:actions user-perm)
        resource-entities (:entities resource-perm)
        user-entities (:entities user-perm)]

    (if (contains? user-actions wildcard-token)
      true
      (if (some #(contains? user-actions %) resource-actions)
        (if (contains? user-entities wildcard-token)
          true
          (some #(contains? user-entities %) resource-entities))
        false
      ))))

(defn- single-permission-implied-by?
  [^Permission resource-perm ^Permission user-perm]

  (cond

    (= (:domain user-perm) wildcard-token) true

    (= (:domain user-perm) (:domain resource-perm))
    (actions-and-entities-implied-by? resource-perm user-perm)

    :else false))


(defn implied-by?
  "Checks if resource-permission (the permission of an entity/resource)
  is implied by user-permission - the permission the user holds."

  [^Permission resource-perm user-perm]

  (cond
    (instance? Permission user-perm)
    (single-permission-implied-by? (make-permission resource-perm) user-perm)

    (or (set? user-perm) (sequential? user-perm))
    (some #(implied-by? resource-perm %) user-perm)

    :else false))

