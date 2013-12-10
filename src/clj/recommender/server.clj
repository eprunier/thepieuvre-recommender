(ns recommender.server
  (:require [thrift-clj.core :as thrift]
            [recommender.service.db :as db]))

;;
;; ## Thrift service implementation
;;

(thrift/import
 (:types [recommender.service Article])
 (:services [recommender.service ArticlesService]))

(defn get-articles
  "Get articles by IDs for the given username."
  [username articles-ids]
  (let [articles-set (set articles-ids)
        read-articles-set (->> (for [article-id articles-ids]
                                 (when (db/read? username article-id)
                                   article-id))
                               (filter #(not (nil? %)))
                               set)
        unread-articles-set (clojure.set/difference articles-set read-articles-set)]
    (-> []
        (conj (map #(Article. % true 0) read-articles-set))
        (conj (map #(Article. % false 0) unread-articles-set))
        (#(apply concat %)))))

(thrift/defservice articles-service
  ArticlesService
  (getArticles [username articles-ids]
               (get-articles username articles-ids))
  (addArticle [username article]
              (db/add-article username article))
  (getReadArticles [username]
                   (db/get-read-articles username))
  (isRead [username article-id]
          (db/read? username article-id)))


;;
;; ## Thrift server management
;;

(def ^:dynamic *server*)

(defn start!
  "Start Thrift server."
  [host port]
  (let [server (-> articles-service
                   (thrift/multi-threaded-server port
                                                 :bind host
                                                 :protocol :compact)
                   thrift/serve!)]
    (alter-var-root #'*server*
                    (constantly server))
    (println "Thrift server started on" (str host ":" port))))

(defn stop!
  "Stop Thrift server."
  []
  (when *server*
    (.stop *server*))
  (println "Thrift server stopped"))
