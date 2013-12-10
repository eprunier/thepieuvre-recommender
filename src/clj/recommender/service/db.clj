(ns recommender.service.db
  (:require [qbits.alia :as alia]
            [qbits.alia.uuid :as uuid]
            [qbits.hayt :refer [select where insert values delete columns] :as hayt]))

(def ^:dynamic *db*)

;;
;; ## Connection
;;

(defn connect!
  "Start a connection to the database. The 'hosts' parameter is a vector of Cassandra nodes IPs.

   Exemples:

      (connect [\"127.0.0.1\"] 9042)
      (connect [\"192.168.1.1\" \"192.168.1.2\"] 12345)"
  [hosts port]
  (let [cluster (alia/cluster hosts :port port)
        db (alia/connect cluster "thepieuvre")]
    (alter-var-root #'*db*
                    (constantly db))))

(defn disconnect!
  "Close database connection."
  []
  (when *db*
    (alia/shutdown *db*)
    (alter-var-root #'*db*
                    (constantly nil))))

;;
;; ## Utilities
;;

(defn new-uuid
  "Create a random UUID."
  []
  (uuid/random))

(defn drop-tables
  "Drop all tables."
  []
  (alia/with-session *db*
    (doseq [table [:user_read_articles :article_readers
                   :user_likes :user_dislikes
                   :articles_likes :article_dislikes]]
      (try
        (alia/execute (hayt/drop-table table))
        (catch Exception e
          (println "Error while droping table" table ":" (.getMessage e)))))))

(defn create-tables
  "Creates all tables."
  []
  (alia/with-session *db*
    (alia/execute (hayt/create-table 
                   :user_read_articles
                   (hayt/column-definitions {:username :varchar
                                             :article_id :varchar
                                             :primary-key [:username :article_id]})))
    (alia/execute (hayt/create-table 
                   :article_readers
                   (hayt/column-definitions {:article_id :varchar
                                             :username :varchar
                                             :primary-key [:article_id :username]})))
    (alia/execute (hayt/create-table 
                   :user_likes
                   (hayt/column-definitions {:username :varchar
                                             :article_id :varchar
                                             :primary-key [:username :article_id]})))
    (alia/execute (hayt/create-table 
                   :user_dislikes
                   (hayt/column-definitions {:username :varchar
                                             :article_id :varchar
                                             :primary-key [:username :article_id]})))
    (alia/execute (hayt/create-table 
                   :article_fans
                   (hayt/column-definitions {:article_id :varchar
                                             :username :varchar
                                             :primary-key [:article_id :username]})))))

(defn execute
  "Execute a CQL query."
  [query]
  (alia/execute *db* query))


;;
;; ## Operations
;;

(defn add-like
  "Adds an article to the like list of the user."
  [username article-id]
  (alia/with-session *db*
    (alia/execute (insert :user_likes
                          (values {:username username
                                   :article_id article-id})))
    (alia/execute (delete :user_dislikes
                          (where {:username username
                                  :article_id article-id})))))

(defn add-dislike
  "Adds an article to the dislike list of the user."
  [username article-id]
  (alia/with-session *db*
    (alia/execute (insert :user_dislikes
                          (values {:username username
                                   :article_id article-id})))
    (alia/execute (delete :user_likes
                          (where {:username username
                                  :article_id article-id})))))

(defn remove-liking
  "Removes liking on an article for a user."
  [username article-id]
  (alia/with-session *db*
    (let [conditions (where {:username username
                             :article_id article-id})]
      (alia/execute (delete :user_likes conditions))
      (alia/execute (delete :user_dislikes conditions)))))

(defn set-read
  "Sets an article as read for a user."
  [username article-id]
  (alia/with-session *db*
    (alia/execute (insert :user_read_articles
                          (values {:username username
                                   :article_id article-id})))
    (alia/execute (insert :article_readers
                          (values {:article_id article-id
                                   :username username})))))

(defn set-unread
  "Sets an article as unread for a user."
  [username article-id]
  (alia/with-session *db*
    (alia/execute (delete :user_read_articles
                          (where {:username username
                                  :article_id article-id})))
    (alia/execute (delete :article_readers
                          (where {:article_id article-id
                                  :username username})))))

(defn read?
  [username article-id]
  (let [results (alia/execute *db*
                              (select :user_read_articles
                                      (where {:username username
                                              :article_id article-id})))]
    (pos? (count results))))

(defn add-article
  "Adds an element to the list of articles read by the user."
  [username {:keys [id read like] :as article}]
  (if read
    (do
      (set-read *db* username id)
      (condp = like
        -1 (add-dislike *db* username id)
        1 (add-like *db* username id)
        (remove-liking *db* username id)))
    (set-unread *db* username id)))

(defn get-read-articles
  "Get all read articles for a user."
  [username]
  (map :article_id
       (alia/execute *db*
                     (select :user_read_articles 
                             (where {:username username})))))

(defn get-readers
  "Get readers for the specified article"
  [article-id]
  (alia/execute *db*
                (select :article_readers
                        (where {:article_id article-id}))))

(defn get-likes
  "Get user likes list."
  [username]
  (alia/execute *db*
                (select :user_likes
                        (where {:username username}))))

(defn get-dislikes
  "Get user dislikes list."
  [username]
  (alia/execute *db*
                (select :user_dislikes
                        (where {:username username}))))

(defn get-fans
  "Returns fans for an article."
  [article-id]
  (alia/execute *db*
                (select :article_fans
                        (where {:article_id article-id}))))
