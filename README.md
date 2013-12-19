# ThePieuvre Recommender

This project aims to provide a recommender system for [The Pieuvre].

[The Pieuvre]: http://thepieuvre.com


## Requirements

### Apache Cassandra

Install Apache [Cassandra] 2.0 and run it.

[Cassandra]: http://cassandra.apache.org

### Redis

Install [Redis] and run it.

[Redis]: http://redis.io

### Thrift

Install Apache [Thrift] 0.9.0 and place the Thrift compiler on your system path.

[Thrift]: http://thrift.apache.org

### Leiningen

[Leiningen] is a build tool for Clojure projects.
Download the 'lein' script and place it on your system path.

[Leiningen]: http://leiningen.org


## Running

Generate Thrift classes:

	lein with-profile thriftc thriftc

Run the service:

	lein run [thrift-host <host>] [thrift-port <port>] 
		[cassandra-hosts <hosts>] [cassandra-port <port>] 
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


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/eprunier/thepieuvre-recommender/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

