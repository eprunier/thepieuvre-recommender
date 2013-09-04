(ns recommender.core
  (:require [recommender.service.db :as db]
            [recommender.server :as server]))

(defn system
  []
  (let [db (db/connect)]
    {:db db
     :server (server/create db)}))

(defn start
  [system]
  (server/start (:server system) system))

(defn stop
  [system]
  (db/disconnect (:db system))
  (server/stop (:server system))
  (dissoc system :db :server))
