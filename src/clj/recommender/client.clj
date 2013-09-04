(ns recommender.client
  (:require [thrift-clj.core :as thrift]))

(thrift/import
 (:types [recommender.service Article])
 (:clients [recommender.service ArticlesService]))

(defn- get-connection
  "Creates a connection to the thrift server."
  ([]
     (get-connection "127.0.0.1" 7007))
  ([host port]
     (thrift/connect! ArticlesService [host port])))

(defn add-article
  [username id read like]
  (with-open [connexion (get-connection)]
    (let [article (Article. id read like)]
      (ArticlesService/addArticle connexion username article))))

(defn get-read-articles
  [username]
  (with-open [connexion (get-connection)]
    (ArticlesService/getReadArticles connexion username)))

(defn get-articles
  [username articles-ids]
  (with-open [connexion (get-connection)]
    (ArticlesService/getArticles connexion username articles-ids)))

(defn read?
  [username article-id]
  (with-open [connexion (get-connection)]
    (ArticlesService/isRead connexion username article-id)))
