(ns agynamix.roles
  (:require
    #?@(:clj [[agynamix.permissions :as p]]
        :cljs [[agynamix.permissions :as p :refer [Permission]]])
    [clojure.set :refer [union]])
  #?(:clj
     (:import [agynamix.permissions Permission])))

(def role-mapping (atom {}))
(def resolvers (atom {}))

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

(defn roles->permissions [roles]
  (let [f (into {} (filter (fn [[k _]] (some #(= k %) roles)) @role-mapping))
        s (apply union (vals f))]
    s))

(defn default-permission-resolver [perm-or-str]
  perm-or-str)

(defn default-role-resolver [role-names]
  role-names)

(defn- permission-resolver [perm-or-str]
  ((:permission-resolver @resolvers) perm-or-str))

(defn- role-resolver [role-name]
  ((:role-resolver @resolvers) role-name))

(defn init-roles
  "Takes your systems role definitions which is a map where the keys are the role names
  and the value is a set of permissions (either as permission records or as strings
  that can be converted into a permission.
  This needs to be initialized PRIOR to any permission checks."
  ([role-map]
   (init-roles role-map default-permission-resolver default-role-resolver))
  ([role-map permission-resolver role-resolver]
   (reset! role-mapping (sanitize-role-map role-map))
   (reset! resolvers {:permission-resolver permission-resolver
                      :role-resolver role-resolver})))

(defn has-permission?
  ([user-map perm]
   (has-permission? user-map :roles :permissions perm))

  ([user-map role-key perm-key perm]
   (let [roles (role-resolver (get user-map role-key #{}))
         role-perms (roles->permissions roles)
         permissions (get user-map perm-key #{})
         sani-perms (map p/make-permission permissions)
         all-perms (union role-perms sani-perms)
         ;_ (println "all permissions " (map #(str %) all-perms))
         resource-perm (permission-resolver perm)]

     (if (or (set? resource-perm) (sequential? resource-perm))
       (every? #(p/implied-by? % all-perms) resource-perm)
       (p/implied-by? resource-perm all-perms)))))


(defn lacks-permission?
  ([user-map perm]
   (lacks-permission? user-map :roles :permissions perm))
  ([user-map role-key perm-key perm]
   (not (has-permission? user-map role-key perm-key perm))))