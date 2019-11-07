(ns meuse.inject
  (:require [meuse.api.crate.http :refer [crates-api!]]
            [meuse.api.crate.new :as new]
            [meuse.api.crate.owner :as owner]
            [meuse.api.crate.search :as search]
            [meuse.api.crate.yank :as yank]
            [meuse.api.meuse.http :refer [meuse-api!]]
            [meuse.api.meuse.category :as category]
            [meuse.api.meuse.crate :as crate]
            [meuse.api.meuse.token :as token]
            [meuse.api.meuse.user :as user]
            [meuse.db.public.category :refer [category-db]]
            [meuse.db.public.crate :refer [crate-db]]
            [meuse.db.public.crate-user :refer [crate-user-db]]
            [meuse.db.public.crate-version :refer [crate-version-db]]
            [meuse.db.public.search :refer [search-db]]
            [meuse.db.public.token :refer [token-db]]
            [meuse.db.public.user :refer [user-db]]
            [meuse.git :refer [git]]))

(defn inject-meuse-api!
  "Inject multimethods to handle HTTP requests for the Meuse API"
  []
  (do
    ;; category
    (defmethod meuse-api! :new-category
      [request]
      (category/new-category category-db request))
    (defmethod meuse-api! :list-categories
      [request]
      (category/list-categories category-db request))
    (defmethod meuse-api! :update-category
      [request]
      (category/update-category category-db request))

    ;; crate
    (defmethod meuse-api! :list-crates
      [request]
      (crate/list-crates crate-db request))

    (defmethod meuse-api! :get-crate
      [request]
      (crate/get-crate category-db crate-db request))

    (defmethod meuse-api! :check-crates
      [request]
      (crate/check-crates crate-db git request))

    ;; token

    (defmethod meuse-api! :delete-token
      [request]
      (token/delete-token token-db request))

    (defmethod meuse-api! :create-token
      [request]
      (token/create-token user-db token-db request))

    (defmethod meuse-api! :list-tokens
      [request]
      (token/list-tokens token-db request))

    ;; user

    (defmethod meuse-api! :new-user
      [request]
      (user/new-user user-db request))

    (defmethod meuse-api! :delete-user
      [request]
      (user/delete-user user-db request))

    (defmethod meuse-api! :update-user
      [request]
      (user/update-user user-db request))

    (defmethod meuse-api! :list-users
      [request]
      (user/list-users user-db request))))

(defn inject-crate-api!
  "Inject multimethods to handle HTTP requests for the Crate API"
  []
  (do
    ;; crate
    (defmethod crates-api! :new
      [request]
      (new/new crate-db git request))

    (defmethod crates-api! :yank
      [request]
      (yank/yank crate-user-db crate-version-db git request))

    (defmethod crates-api! :unyank
      [request]
      (yank/unyank crate-user-db crate-version-db git request))

    ;; owner

    (defmethod crates-api! :add-owner
      [request]
      (owner/add-owner crate-user-db request))

    (defmethod crates-api! :remove-owner
      [request]
      (owner/remove-owner crate-user-db request))

    (defmethod crates-api! :list-owners
      [request]
      (owner/list-owners user-db request))

    ;; search

    (defmethod crates-api! :search
      [request]
      (search/search search-db request))))

(defn inject!
  []
  (inject-crate-api!)
  (inject-meuse-api!))