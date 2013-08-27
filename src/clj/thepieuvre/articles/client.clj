(ns thepieuvre.articles.client
  (:require [thrift-clj.core :as thrift]))

(thrift/import
 (:types [thepieuvre.articles.service User Article])
 (:clients [thepieuvre.articles.service ArticlesService]))

(defn- add-read-article
  [connexion user-login article-id article-like]
  (let [user (User. user-login)
        article (Article. article-id article-like)]
    (ArticlesService/addReadArticle connexion user article)))

(defn- get-read-articles
  [connexion user-login]
  (let [user (User. user-login)]
    (ArticlesService/getReadArticles connexion user)))

(defn- dispatch-cmd
  [connexion cmd params]
  (case cmd
    "add-read-article" (let [[user-login article-id article-like] params]
                         (add-read-article connexion
                                           user-login
                                           article-id
                                           article-like))
    "get-read-articles" (let [[user-login] params]
                          (get-read-articles connexion
                                             user-login))))

(defn run-cmd
  [host port cmd & params]
  (with-open [connexion (thrift/connect! ArticlesService [host port])]
    (dispatch-cmd connexion cmd params)))
