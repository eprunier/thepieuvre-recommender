(ns thepieuvre.articles.system
  (:require [thepieuvre.articles.service.db :as db]))

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
