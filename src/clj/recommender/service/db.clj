(ns recommender.service.db
  (:require [qbits.alia :as alia]
            [qbits.alia.uuid :as uuid]
            [qbits.hayt :refer [select where insert values delete columns] :as hayt]))

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
  "Close database connection."
  [session]
  (when session
    (alia/shutdown session)))

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
    (doseq [table [:user_read_articles :article_readers
                   :user_likes :user_dislikes
                   :articles_likes :article_dislikes]]
      (try
        (alia/execute (hayt/drop-table table))
        (catch Exception e
          (println "Error while droping table" table ":" (.getMessage e)))))))

(defn create-tables
  "Creates all tables."
  [session]
  (alia/with-session session
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
  [session query]
  (alia/execute session query))


;;
;; ## Operations
;;

(defn add-like
  "Adds an article to the like list of the user."
  [session username article-id]
  (alia/with-session session
    (alia/execute (insert :user_likes
                          (values {:username username
                                   :article_id article-id})))
    (alia/execute (delete :user_dislikes
                          (where {:username username
                                  :article_id article-id})))))

(defn add-dislike
  "Adds an article to the dislike list of the user."
  [session username article-id]
  (alia/with-session session
    (alia/execute (insert :user_dislikes
                          (values {:username username
                                   :article_id article-id})))
    (alia/execute (delete :user_likes
                          (where {:username username
                                  :article_id article-id})))))

(defn remove-liking
  "Removes liking on an article for a user."
  [session username article-id]
  (alia/with-session session
    (let [conditions (where {:username username
                             :article_id article-id})]
      (alia/execute (delete :user_likes conditions))
      (alia/execute (delete :user_dislikes conditions)))))

(defn set-read
  "Sets an article as read for a user."
  [session username article-id]
  (alia/with-session session
    (alia/execute (insert :user_read_articles
                          (values {:username username
                                   :article_id article-id})))
    (alia/execute (insert :article_readers
                          (values {:article_id article-id
                                   :username username})))))

(defn set-unread
  "Sets an article as unread for a user."
  [session username article-id]
  (alia/with-session session
    (alia/execute (delete :user_read_articles
                          (where {:username username
                                  :article_id article-id})))
    (alia/execute (delete :article_readers
                          (where {:article_id article-id
                                  :username username})))))

(defn read?
  [session username article-id]
  (let [results (alia/execute session
                              (select :user_read_articles
                                      (where {:username username
                                              :article_id article-id})))]
    (pos? (count results))))

(defn add-article
  "Adds an element to the list of articles read by the user."
  [session username {:keys [id read like] :as article}]
  (if read
    (do
      (set-read session username id)
      (condp = like
        -1 (add-dislike session username id)
        1 (add-like session username id)
        (remove-liking session username id)))
    (set-unread session username id)))

(defn get-read-articles
  "Get all read articles for a user."
  [session username]
  (map :article_id
       (alia/execute session 
                     (select :user_read_articles 
                             (where {:username username})))))

(defn find-articles
  [session username articles-ids]
  (map :article_id
       (alia/execute session
                     (select :user_read_articles
                             (where {:username username
                                     :article_id [:in articles-ids]})))))

(defn get-readers
  "Get readers for the specified article"
  [session article-id]
  (alia/execute session
                (select :article_readers
                        (where {:article_id article-id}))))

(defn get-likes
  "Get user likes list."
  [session username]
  (alia/execute session
                (select :user_likes
                        (where {:username username}))))

(defn get-dislikes
  "Get user dislikes list."
  [session username]
  (alia/execute session
                (select :user_dislikes
                        (where {:username username}))))

(defn get-fans
  "Returns fans for an article."
  [session article-id]
  (alia/execute session
                (select :article_fans
                        (where {:article_id article-id}))))