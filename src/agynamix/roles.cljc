(ns agynamix.roles
  (:require
    #?@(:clj [[agynamix.permissions :as p]]
        :cljs [[agynamix.permissions :as p :refer [Permission]]])
    [clojure.set :refer [union]])
  #?(:clj
     (:import [agynamix.permissions Permission])))

(declare default-permission-resolver
         default-role-resolver)

(def role-mapping (atom {}))
(def resolvers (atom {:permission-resolver default-permission-resolver
                      :role-resolver default-role-resolver}))

;(defn- sanitize-permission [perm-str]
;  (cond
;    (string? perm-str) (p/make-permission perm-str)
;    (keyword? perm-str) (p/make-permission (name perm-str))
;    (instance? Permission perm-str) perm-str
;
;    :else p/empty-permission))

(defn- sanitize-role-map-value
  "Checks the value(s) of a role-map entry
  If they reference another role recursively call this function with the value of
  the found role key, else convert the value into a permission"
  [role-map val]
  (cond
    (string? val)
    (if-let [role-val (get role-map val)]
      (sanitize-role-map-value role-map role-val)
      [(p/make-permission val)])

    (keyword? val)
    (if-let [role-val (get role-map val)]
      (sanitize-role-map-value role-map role-val)
      [(p/make-permission (name val))])

    (instance? Permission val) #{val}
    (or (seq? val) (set? val))
    (into [] (map (partial sanitize-role-map-value role-map) val))

    :else []))

(defn- sanitize-role-map [role-map]
  (into {} (map
             (fn [[key val]]
               [key (into #{}
                          (flatten
                            (sanitize-role-map-value role-map val)))]) role-map)))

(defn roles->permissions
  "Resolves a seq of roles into ints contained permissions.
  If a role contains other roles as values, will recursively resolve these until
  it ends up at the leafes, the permissions."
  [roles]

  (let [f (into {}
                (filter
                  (fn [[k _]] (some #(= k %) roles))
                  @role-mapping))

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


(defn permission-check-fn
  [m role-key perm-key perm check-fn]
  (let [roles (role-resolver (get m role-key #{}))
        role-perms (roles->permissions roles)
        permissions (get m perm-key #{})
        sani-perms (map p/make-permission permissions)
        all-perms (union role-perms sani-perms)
        ;_ (println "all permissions " (map #(str %) all-perms))
        resource-perm (permission-resolver perm)]

    (check-fn all-perms resource-perm)))


(defn has-permission?
  ([m perm]
   (has-permission? m :roles :permissions perm))

  ([m role-key perm-key perm]
   (permission-check-fn m role-key perm-key perm
                      (fn [all-perms resource-perm]
                        (if (or (set? resource-perm) (sequential? resource-perm))
                          (every? #(p/implied-by? % all-perms) resource-perm)
                          (p/implied-by? resource-perm all-perms))))))

(defn has-any-permission?
  ([m perm]
   (has-any-permission? m :roles :permissions perm))

  ([m role-key perm-key perm]
   (permission-check-fn m role-key perm-key perm
                      (fn [all-perms resource-perm]
                        (if (or (set? resource-perm) (sequential? resource-perm))
                          (some #(p/implied-by? % all-perms) resource-perm)
                          (p/implied-by? resource-perm all-perms))))))


(defn lacks-permission?
  ([m perm]
   (lacks-permission? m :roles :permissions perm))
  ([m role-key perm-key perm]
   (not (has-permission? m role-key perm-key perm))))

(defn lacks-all-permissions?
  ([m perm]
   (lacks-all-permissions? m :roles :permissions perm))
  ([m role-key perm-key perm]
   (not (has-any-permission? m role-key perm-key perm))))

