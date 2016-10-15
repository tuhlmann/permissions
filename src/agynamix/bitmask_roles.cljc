(ns agynamix.bitmask-roles
  (:require
    #?@(:clj [[agynamix.permissions :as p]]
        :cljs [[agynamix.permissions :as p :refer [Permission]]])
              [clojure.set :refer [union]]
              [agynamix.roles :as r])
  #?(:clj
     (:import [agynamix.permissions Permission]
              [java.lang.Math])))

(defn- two-to-power-of [x]
  #?(:clj
     (long (Math/pow 2 x)))

  #?(:cljs
     (long (js/Math.pow 2 x))))

(defn- role-name-to-bitmask [role-name]
  (let [v (map
            (fn [pos]
              (if (not= (bit-and role-name (bit-shift-left 1 pos)) 0)
                (two-to-power-of pos)
                0))
            (range 0 64))
        v (filter #(not= 0 %) v)
        v (into #{} (doall v))]
    v))


(defn bitmask-role-resolver
  "Flattens one or more bitmask values into a set of numbers with only a single bit set.
  For instance 5 would be flattened into #{1 2 4}"
  [role-names]

  (if (or (set? role-names) (sequential? role-names))
    (into #{} (mapcat role-name-to-bitmask role-names))

    (role-name-to-bitmask role-names)))

(defn bitmask-permission-resolver
  "Takes one or more number values, flattens them into their bitmask values, and creates a flat
  set of permissions associated to thise bitmask values interpreted as roles"
  [role-names]

  (let [permissions (cond
                      (instance? Permission role-names) #{role-names}
                      (string? role-names) #{(p/make-permission role-names)}

                      :else (r/roles->permissions (bitmask-role-resolver role-names)))]

    permissions))
