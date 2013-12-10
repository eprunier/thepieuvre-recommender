(ns recommender.client
  (:require [thrift-clj.core :as thrift]))

(thrift/import
 (:types [recommender.service Article])
 (:clients [recommender.service ArticlesService]))


;;
;; ## Connection management
;;

(def ^:dynamic *connection*)

(defn connect!
  "Open Thrift connection."
  ([]
     (connect! "127.0.0.1" 7007))
  ([host port]
     (let [connection (thrift/connect! ArticlesService [host port])]
       (alter-var-root #'*connection*
                       (constantly connection)))))

(defn disconnect!
  "Close Thrift connection."
  []
  (when *connection*
    (thrift/disconnect! *connection*)))


;;
;; ## Functions provided by the server
;;

(defn add-article
  [username id read like]
  (let [article (Article. id read like)]
    (ArticlesService/addArticle *connection* username article)))

(defn get-read-articles
  [username]
  (ArticlesService/getReadArticles *connection* username))

(defn get-articles
  [username articles-ids]
  (ArticlesService/getArticles *connection* username articles-ids))

(defn read?
  [username article-id]
  (ArticlesService/isRead *connection* username article-id))
