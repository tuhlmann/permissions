(ns agynamix.roles
  (:require
    #?@(:clj [[agynamix.permissions :as p]]
        :cljs [[agynamix.permissions :as p :refer [Permission]]])
    [clojure.set :refer [union]])
  #?(:clj
     (:import [agynamix.permissions Permission])))

(defonce role-mapping (atom {}))

(defn- sanitize-permission [perm-str]
  (cond
    (string? perm-str) (p/make-permission perm-str)
    (keyword? perm-str) (p/make-permission (name perm-str))
    (instance? Permission perm-str) perm-str

    :else p/empty-permission))

(defn- sanitize-role-map [role-map]
  (into {} (map (fn [[key val]]
         [key (cond
           (string? val) #{(p/make-permission val)}
           (keyword? val) #{(p/make-permission (name val))}
           (instance? Permission val) #{val}
           (or (seq? val) (set? val))
           (into #{} (map sanitize-permission val))

           :else #{})]) role-map)))

(defn- roles->permissions [roles]
  (let [f (into {} (filter (fn [[k _]] (some #(= k %) roles)) @role-mapping))
        s (union (vals f))]
    s))

(defn init-roles
  "Takes your systems role definitions which is a map where the keys are the role names
  and the value is a set of permissions (either as permission records or as strings
  that can be converted into a permission.
  This needs to be initialized PRIOR to any permission checks."
  [role-map]

  (reset! role-mapping (sanitize-role-map role-map)))

(defn has-permission?
  ([user-map perm]
   (has-permission? user-map :roles :permissions perm))

  ([user-map role-key perm-key perm]
   (let [roles (get user-map role-key #{})
         role-perms (roles->permissions roles)
         permissions (get user-map perm-key #{})
         sani-perms (map p/make-permission permissions)
         all-perms (union role-perms sani-perms)]

     (p/implied-by? perm all-perms))))


(defn lacks-permission?
  ([user-map perm]
   (lacks-permission? user-map :roles :permissions perm))
  ([user-map role-key perm-key perm]
   (not (has-permission? user-map role-key perm-key perm))))