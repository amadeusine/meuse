(ns meuse.git
  "Interacts with a git repository"
  (:require [meuse.config :refer [config]]
            [meuse.metric :as metric]
            [exoscale.ex :as ex]
            [mount.core :refer [defstate]]
            [clojure.java.shell :as shell]
            [clojure.tools.logging :refer [debug]]
            [clojure.string :as string]))

(defprotocol Git
  (add [this])
  (commit [this msg-header msg-body])
  (get-lock [this])
  (git-cmd [this args])
  (pull [this])
  (push [this]))

(defrecord LocalRepository [path target lock]
  Git
  (add [this]
    (git-cmd this ["add" "."]))
  (commit [this msg-header msg-body]
    (git-cmd this ["commit" "-m" msg-header "-m" msg-body]))
  (get-lock [this]
    lock)
  (git-cmd [this args]
    (debug "git command" (string/join " " args))
    (metric/with-time :git.local ["command" (first args)]
      (let [result (apply shell/sh "git" "-C" path args)]
        (debug "git command status code=" (:exit result)
               "out=" (:out result)
               "err=" (:err result))
        (when-not (= 0 (:exit result))
          (throw (ex/ex-fault "error executing git command"
                              {:exit-code (:exit result)
                               :stdout (:out result)
                               :stderr (:err result)
                               :command args}))))))
  (push [this]
    (git-cmd this (concat ["push"] (string/split target #"/"))))
  (pull [this]
    (git-cmd this ["pull" target])))

(defstate git
  :start (map->LocalRepository (merge (:metadata config)
                                      {:lock (java.lang.Object.)})))
