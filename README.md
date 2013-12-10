# ThePieuvre Recommender Service

This project aims to provide a recommender system for [The Pieuvre].

[The Pieuvre]: http://thepieuvre.com


## Requirements

### Apache Cassandra

Install Apache Cassandra 2.0 and run it.

### Thrift

Install Apache [Thrift] 0.9.1 and place the Thrift compiler on your system path.

[Thrift]: http://thrift.apache.org

### Leiningen

[Leiningen] is the build tool for Clojure projects.
Download the 'lein' script and place it on your system path.

[Leiningen]: http://leiningen.org


## Running

Run the service with [Leiningen]:

   lein run [thrift-host <host>] [thrift-port <port>] \
	   [cassandra-hosts <hosts>] [cassandra-port <port>] \
	   [redis-host <host>] [redis-port <port>]

The Recommender Service is now listening on port 7007 (or the given thrift-port) 
for Thrift requests and on a redis queue named 'queue:recommender'.


## Copyright and License

Copyright © 2013 Eric Prunier.

The use and distribution terms for this software are covered by the
[Eclipse Public License 1.0] which can be found in the file
epl-v10.html at the root of this distribution. By using this software
in any fashion, you are agreeing to be bound by the terms of this
license. You must not remove this notice, or any other, from this
software.

[Eclipse Public License 1.0]: http://opensource.org/licenses/eclipse-1.0.php
