(ns thepieuvre.user-articles
  (:require [thepieuvre.user-articles.services.db :as db]))

(defn system
  []
  {:db nil})

(defn start
  [s]
  (let [db (db/connect)]
    (assoc s :db db)))

(defn stop
  [s]
  (db/disconnect (:db s))
  (assoc s :db nil))
