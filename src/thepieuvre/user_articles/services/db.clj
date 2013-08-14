(ns thepieuvre.user-articles.services.db
  (:require [clojurewerkz.cassaforte.client :as client]
            [clojurewerkz.cassaforte.multi.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]))

(defn connect
  [{:keys [hosts port] :as db-specs :or {hosts ["127.0.0.1"]
                                         port 9042}}]
  (let [db (-> {:contact-points hosts
                :port port}
               client/build-cluster
               (client/connect :thepieuvre))]
    (cql/use-keyspace db :thepieuvre)
    db))

(defn init
  [db]
  (cql/create-table db
                    "Users"
                    (query/column-definitions {:name :varchar
                                             :primary-key [:name]})))

(defn add-user
  [db user]
  (cql/insert db "Users" user))

(defn execute
  [db query]
  (client/execute db query))