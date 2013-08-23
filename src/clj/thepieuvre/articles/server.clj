(ns thepieuvre.articles.server
  (:require [thrift-clj.core :as thrift]
            [thepieuvre.articles.service.db :as db]))

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
                  (db/add-read-article user article))
  (getReadArticles [user]
                   (db/get-read-articles user)))

;;
;; ## Service launcher
;;

(defn run-server
  [server-fn host port]
  (let [server (server-fn articles-service port :bind host)]
    (println "Thrift server started on" (str host ":" port))
    (thrift/serve-and-block! server)))

(defn -main
  [& {:keys [host port]
      :or {host "127.0.0.1"
           port "7007"}}]
  (run-server thrift/multi-threaded-server
              host
              (Integer/parseInt port)))
