(ns thepieuvre.articles.server
  (:require [thrift-clj.core :as thrift]
            [thepieuvre.articles.service.db :as db]))

(def db)

;;
;; ## Thrift imports
;;

(thrift/import
 (:types [thepieuvre.articles.service User Article])
 (:services [thepieuvre.articles.service ArticlesService]))

;;
;; ## Thrift service implementation
;;

(thrift/defservice articles-service
  ArticlesService
  (addReadArticle [user article]
                  (db/add-read-article db user article))
  (getReadArticles [user]
                   (db/get-read-articles db user)))

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
                                :bind host))

(defn start
  [server system]
  (alter-var-root #'db
                  (constantly (:db system)))
  (let [server (thrift/serve! server)]
    (println "Service Thrift started")
    (assoc system
      :server server)))

(defn stop
  [server]
  (.stop server))
