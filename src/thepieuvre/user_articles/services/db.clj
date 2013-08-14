(ns thepieuvre.user-articles.services.db
  (:require [clojurewerkz.cassaforte.client :as client]
            [clojurewerkz.cassaforte.multi.cql :as cql]
            [clojurewerkz.cassaforte.query :as query]))

;;
;; ## DB connection
;;

(defn connect
  "Creates a connection to the Database.

   Optional parameters:

      :hosts -> vector of Cassandra nodes IPs
                (default [\"127.0.0.1\"])
      :port -> Cassandra port
               (default 9042)

   Exemples:

      (connect)
      (connect :hosts [\"192.168.1.1\"])
      (connect :port 12345)
      (connect :hosts [\"192.168.1.1\" \"192.168.1.2\"]
               :port 12345)"
  [& {:keys [hosts port] :as db-specs :or {hosts ["127.0.0.1"]
                                           port 9042}}]
  (println hosts)
  (println port)
  (let [db (-> {:contact-points hosts
                :port port}
               client/build-cluster
               (client/connect :thepieuvre))]
    db))

;;
;; ## Utilities for tables creation and CQL queries
;;

(defn create-tables
  "Creates all tables."
  [session]
  (cql/create-table session
                    :users
                    (query/column-definitions {:email :varchar
                                               :password :varchar
                                               :first_name :varchar
                                               :last_name :varchar
                                               :primary-key [:email]}))
  (cql/create-table session
                    :articles
                    (query/column-definitions {:article_id :uuid
                                               :feed_id :uuid
                                               :primary-key [:article_id]}))
  (cql/create-table session
                    :user_articles
                    (query/column-definitions {:user_email :varchar
                                               :article_id :uuid
                                               :feed_id :uuid
                                               :primary-key [:user_email :article_id]}))
  (cql/create-table session
                    :read_articles
                    (query/column-definitions {:user_email :varchar
                                               :article_id :uuid
                                               :feed_id :uuid
                                               :primary-key [:user_email :article_id]})))

(defn execute
  "Execute a CQL query."
  [session query]
  (client/execute session query))

;;
;; ## User operations
;;

(defn add-user
  [session user]
  (cql/insert session :users user))

(defn get-user
  [session email]
  (cql/select session :users (query/where :email email)))

(defn get-all-users
  [session]
  (cql/select session :users))