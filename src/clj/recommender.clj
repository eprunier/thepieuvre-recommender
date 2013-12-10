(ns recommender
  (:require [recommender.service.db :as db]
            [recommender.server :as server]))

(defn system
  []
  {:thrift-host "127.0.0.1"
   :thrift-port 7007
   :cassandra-hosts ["127.0.0.1"]
   :cassandra-port 9042
   :redis-host "127.0.0.1"
   :redis-port 6379})

(defn start
  "Start the system."
  [system]
  (db/connect! (:cassandra-hosts system)
               (:cassandra-port system))
  (server/start! (:thrift-host system)
                 (:thrift-port system)))

(defn stop
  "Stop the system."
  [system]
  (db/disconnect!)
  (server/stop!))

(defn -main
  [& {:as args}]
  (start (system)))
