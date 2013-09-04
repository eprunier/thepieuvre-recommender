(ns recommender.server
  (:require [thrift-clj.core :as thrift]
            [recommender.service.db :as db]))

(def ^:dynamic *db*)

;;
;; ## Thrift imports
;;

(thrift/import
 (:types [recommender.service Article])
 (:services [recommender.service ArticlesService]))


;;
;; ## Methods
;;

(defn get-articles
  [username articles-ids]
  (let [articles-set (set articles-ids)
        read-articles-set (->> articles-set
                               (db/find-articles *db* username)
                               set)
        unread-articles-set (clojure.set/difference articles-set read-articles-set)]
    (-> []
        (conj (map #(Article. % true 0) read-articles-set))
        (conj (map #(Article. % false 0) unread-articles-set))
        (#(apply concat %)))))

;;
;; ## Thrift service implementation
;;

(thrift/defservice articles-service
  ArticlesService
  (getArticles [username articles-ids]
               (let [results (get-articles username articles-ids)]
                 (println "results:" results)
                 results))
  (addArticle [username article]
              (db/add-article *db* username article))
  (getReadArticles [username]
                   (db/get-read-articles *db* username))
  (isRead [username article-id]
          (db/read? *db* username article-id)))

;;
;; ## Service launcher
;;

(defn create
  [db & {:keys [host port]
             :or {host "127.0.0.1"
                  port "7007"}}]
  (println "Creating Thrift service on" (str host ":" port))
  (thrift/multi-threaded-server articles-service
                                (Integer/parseInt port)
                                :bind host
                                :protocol :compact))

(defn start
  [server system]
  (alter-var-root #'*db*
                  (constantly (:db system)))
  (let [server (thrift/serve! server)]
    (println "Service Thrift started")
    (assoc system
      :server server)))

(defn stop
  [server]
  (.stop server))
