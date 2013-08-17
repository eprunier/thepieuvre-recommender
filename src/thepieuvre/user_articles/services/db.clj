(ns thepieuvre.user-articles.services.db
  (:require [qbits.alia :as alia]
            [qbits.alia.uuid :as uuid]
            [qbits.hayt :refer [select insert where] :as hayt]))

;;
;; ## Connection
;;

(defn connect
  "Creates a connection to the Database and returns a session.

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
  [& {:keys [hosts port]
      :or {hosts ["127.0.0.1"]
           port 9042}}]
  (let [cluster (alia/cluster hosts :port port)]
    (alia/connect cluster "thepieuvre")))

(defn disconnect
  [session]
  (alia/shutdown session))

;;
;; ## Utilities
;;

(defn new-uuid
  "Create a random UUID."
  []
  (uuid/random))

(defn drop-tables
  "Drop all tables."
  [session]
  (alia/with-session session
    (doseq [table [:users :articles :read_articles :user_articles]]
     (alia/execute (hayt/drop-table table)))))

(defn create-tables
  "Creates all tables."
  [session]
  (alia/with-session session
    (alia/execute (hayt/create-table 
                   :users
                   (hayt/column-definitions {:email :varchar
                                             :password :varchar
                                             :first_name :varchar
                                             :last_name :varchar
                                             :primary-key [:email]})))
    (alia/execute (hayt/create-table
                   :articles
                   (hayt/column-definitions {:id :uuid
                                             :feed_id :uuid
                                             :date :timestamp
                                             :article_title :varchar
                                             :primary-key [:id]})))
    (alia/execute (hayt/create-table 
                   :read_articles
                   (hayt/column-definitions {:user_email :varchar
                                             :article_id :uuid
                                             :primary-key [:user_email :article_id]})))  
    (alia/execute (hayt/create-table 
                   :user_articles
                   (hayt/column-definitions {:user_email :varchar
                                             :article_id :uuid
                                             :primary-key [:user_email :article_id]})))))

(defn execute
  "Execute a CQL query."
  [session query]
  (alia/execute session query))


;;
;; ## User operations
;;

(defn add-user
  "Add a new user."
  [session user]
  (alia/execute session (insert :users user)))

(defn get-user
  "Get a user by is email address."
  [session email]
  (alia/execute (select session :users (where {:email email}))))

(defn get-all-users
  "Get all users."
  [session]
  (alia/execute session (select :users)))


;;
;; ## Article operations
;;

(defn add-article
  [session article]
  (alia/execute session (insert :articles article)))

(defn get-all-articles
  [session]
  (alia/execute session (select :articles)))

(defn add-read-article
  [session user article]
  (let [statement (insert  :read-articles
                                 {:user_email (:email user)
                                  :article_id (:id article)})]
    (println "statement: " statement))
  (alia/execute session (insert  :read-articles
                                 {:user_email (:email user)
                                  :article_id (:id article)})))

(defn get-all-read-articles
  [session]
  (alia/execute session (select :read_articles)))