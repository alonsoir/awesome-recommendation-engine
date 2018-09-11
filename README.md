# Awesome Recommendation Engine

The purpose of this project is to practise the basics about how to code a almost near real time 
rating based recommendation engine. The simple idea is to calculate recomendations of 
different items using notes that other users have given to other products, 
recalculating as quickly as possible, that is, as soon as the note has arrived in the system.

## These are the components:

 - A kafka producer is going to ask periodically to Amazon in order to know what products based on my own ratings 
and i am going to introduced them into some kafka topic.

 - A spark streaming process is going to read from that previous topic.

### Steps 
 
 - Apply some machine learning algorithms ([ALS][1], [content based filtering colaborative filtering][2]) on those datasets readed by 
the spark streaming process.

 - Save results in a mongo or cassandra instance.

 - Use play framework to create an websocket interface between the mongo instance and the visual interface.

 
I am going to use some ideas from a previous work: 

[hello-kafka-twitter-scala](https://github.com/alonsoir/hello-kafka-twitter-scala)

[recomendation-spark-engine](https://github.com/alonsoir/recomendation-spark-engine)

Actually the project can push data to kafka topic, the spark streaming process can recover data from the topic and
save them into mongo instance. Attention, I've only had time to test it with kafka 0.8.1. To use it with later versions, you would have to edit the build.sbt file and change the version of the library. Open a PR in case there are problems and we see it. 

To get kafka and zookeeper up and running, please follow the instructions on this website. https://kafka.apache.org/081/documentation.html#quickstart

To get a mongo instance up and running, please follow the instructions on this website.
https://www.codecademy.com/articles/tdd-setup-mongodb-2

Then, you must create a mongo database instance and a mongo collection with the same data that you are providing in your src/main/resources/references.conf file. To do that, start the server with mongod daemon command, then open a mongo session just typing mongo in another terminal, next you have to run the next commands provided by the instructions provided on this website.
https://www.tutorialspoint.com/mongodb/mongodb_create_database.htm
https://www.tutorialspoint.com/mongodb/mongodb_create_collection.htm

### Possible Troubleshooting with mongo
Maybe, as me, you are in trouble to run again the daemon after upgrading mongo. 
I found useful this thread in Stackoverflow. 
https://stackoverflow.com/questions/21448268/how-to-set-mongod-dbpath

### How to build 
The project uses sbt with pack support to build the unix style commands described aboved:

	$ sbt clean pack
	[info] Loading project definition from /Users/aironman/awesome-recommendation-engine/project
	[info] Set current project to my-recommendation-spark-engine (in build file:/Users/aironman/awesome-recommendation-engine/)
	[success] Total time: 0 s, completed 06-sep-2018 12:12:40
	[info] Updating {file:/Users/aironman/awesome-recommendation-engine/}awesome-recommendation-engine...
	[info] Resolving org.scala-lang#scalap;2.10.4 ...
	[info] Done updating.
	[warn] Scala version was updated by one of library dependencies:
	[warn] 	* org.scala-lang:scala-compiler:2.10.0 -> 2.10.2
	...
	[info] Packaging /Users/aironman/awesome-recommendation-engine/target/scala-2.10/my-recommendation-spark-engine_2.10-1.0-SNAPSHOT.jar ...
	[info] Done packaging.
	[info] Creating a distributable package in target/pack
	...
	[info] Create a bin folder: target/pack/bin
	[info] Generating launch scripts
	[info] main class for twitter-producer: example.producer.TwitterProducer
	[info] Generating target/pack/bin/twitter-producer
	[info] Generating target/pack/bin/twitter-producer.bat
	[info] main class for producer-stream-example: example.producer.ProducerStreamExample
	[info] Generating target/pack/bin/producer-stream-example
	[info] Generating target/pack/bin/producer-stream-example.bat
	[info] main class for amazon-producer-example: example.producer.AmazonProducerExample
	[info] Generating target/pack/bin/amazon-producer-example
	[info] Generating target/pack/bin/amazon-producer-example.bat
	[info] main class for direct-kafka-word-count: example.spark.DirectKafkaWordCount
	[info] Generating target/pack/bin/direct-kafka-word-count
	[info] Generating target/pack/bin/direct-kafka-word-count.bat
	[info] main class for amazon-kafka-connector: example.spark.AmazonKafkaConnector
	[info] Generating target/pack/bin/amazon-kafka-connector
	[info] Generating target/pack/bin/amazon-kafka-connector.bat
	[info] main class for kafka-connector: example.spark.KafkaConnector
	[info] Generating target/pack/bin/kafka-connector
	[info] Generating target/pack/bin/kafka-connector.bat
	[info] packed resource directories = /Users/aironman/awesome-recommendation-engine/src/pack
	[info] Generating target/pack/Makefile
	[info] Generating target/pack/VERSION
	[info] done.
	[success] Total time: 61 s, completed 06-sep-2018 12:13:41

After running sbt clean pack within your source folder, you can see unix styled commands within the target/pack/bin folder.

	$ ls
	LICENSE			activator.properties	log-cleaner.log		ratings.csv		target
	README.md		build.sbt		project			src
	$ cd target/
	$ ls
	pack			resolution-cache	scala-2.10		streams
	$ cd pack/
	$ ls
	Makefile	VERSION		bin		lib
	$ ls bin/
	amazon-kafka-connector		amazon-producer-example		direct-kafka-word-count		kafka-connector			producer-stream-example		twitter-producer
	amazon-kafka-connector.bat	amazon-producer-example.bat	direct-kafka-word-count.bat	kafka-connector.bat		producer-stream-example.bat	twitter-producer.bat
	
Before running the commands, you will need a kafka node running with a topic of your choice. The next command, amazon-kafka-connector is running with a kafka node running in your localhost using the port 9092. The topic is amazonRatingsTopic.

### Actual output:
    $ ./amazon-kafka-connector 127.0.0.1:9092 amazonRatingsTopic
	  Initializing Streaming Spark Context and kafka connector...
	  Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
	  16/05/16 18:48:49 INFO SparkContext: Running Spark version 1.6.1
	  16/05/16 18:48:49 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
	  ...
	  16/05/16 18:48:51 INFO VerifiableProperties: Verifying properties
	  16/05/16 18:48:51 INFO VerifiableProperties: Property group.id is overridden to 
	  16/05/16 18:48:51 INFO VerifiableProperties: Property zookeeper.connect is overridden to 
	  Initialized Streaming Spark Context and kafka connector...
	  Initializing mongodb connector...
	  Initialized mongodb connector...
	  Creating temporary table in mongo instance...
	  16/05/16 18:48:52 INFO SparkContext: Starting job: show at AmazonKafkaConnectorWithMongo.scala:137
	  16/05/16 18:48:53 INFO DAGScheduler: Got job 0 (show at AmazonKafkaConnectorWithMongo.scala:137) with 1 output partitions
	  ...
	  16/05/16 18:48:53 INFO DAGScheduler: Job 0 finished: show at AmazonKafkaConnectorWithMongo.scala:137, took 0,250144 s
	  +--------------------+--------------------+
	  |                  id|       amazonProduct|
	  +--------------------+--------------------+
	  |Mon May 16 18:41:...|[  null  , "{\"it...|
	  |Mon May 16 18:42:...|[  null  , "{\"it...|
	  |Mon May 16 18:45:...|[  null  , "{\"it...|
	  +--------------------+--------------------+
  	16/05/16 18:48:53 INFO BlockManagerInfo: Removed broadcast_0_piece0 on localhost:57536 in memory (size: 2.5 KB, free: 2.4 GB)
	  ...
	  16/05/16 18:48:57 INFO StreamingContext: Invoking stop(stopGracefully=false) from shutdown hook
	  16/05/16 18:48:57 INFO JobGenerator: Stopping JobGenerator immediately
	  16/05/16 18:48:57 INFO RecurringTimer: Stopped timer for JobGenerator after time 1463417336000
	  16/05/16 18:48:57 INFO JobGenerator: Stopped JobGenerator
	  16/05/16 18:48:57 INFO JobScheduler: Stopped JobScheduler
	  Finished!

Next, i am going to ask to amazon for a product using a productId. Of course, this should be a RESTful get petition or something like that, but i implemented a unix command.

	$ ./amazon-producer-example 0981531679
	Trying to parse product with id 0981531679
	amazonProduct is AmazonProduct(0981531679,Scala Puzzlers,http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679,http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg,)
	amazon product sent to kafka cluster...AmazonProduct(0981531679,Scala Puzzlers,http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679,http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg,)

Ok, lets see if the previous output is already saved in a mongo instance...

	$ mongo
	MongoDB shell version: 3.2.6
	connecting to: test
	> use alonsodb;
	switched to db alonsodb
	> db.amazonRatings.find()
	{ "_id" : ObjectId("5739f84a8d6ab41037bbf32d"), "id" : ISODate("2016-05-16T16:41:46.183Z"), "amazonProduct" : [ null, "{\"itemId\":\"0981531679\",\"title\":\"Scala Puzzlers\",\"url\":\"http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679\",\"img\":\"http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg\",\"description\":\"\"}" ] }
	{ "_id" : ObjectId("5739f8628d6ab41037bbf32e"), "id" : ISODate("2016-05-16T16:42:10.025Z"), "amazonProduct" : [ null, "{\"itemId\":\"0981531679\",\"title\":\"Scala Puzzlers\",\"url\":\"http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679\",\"img\":\"http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg\",\"description\":\"\"}" ] }
	{ "_id" : ObjectId("5739f9308d6ab41037bbf32f"), "id" : ISODate("2016-05-16T16:45:36.021Z"), "amazonProduct" : [ null, "{\"itemId\":\"0981531679\",\"title\":\"Scala Puzzlers\",\"url\":\"http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679\",\"img\":\"http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg\",\"description\":\"\"}" ] }
	> 


Yeah, i have data in the mongo instance, i should find if previous data exists with the same content, but that wasnt the most important thing to do, so i let it go for future rewrites.

The idea of the project comes from a course about  bigdata technology that i received from formacionhadoop.com. 
I have to consolidate and to practice with scala, spark streaming, spark-ml, kafka and mongo.
In a future, i would like to rewrite this project from scratch, with the microservices style, restful operations to interact with Amazon, packaging with docker images, kafka, spark streaming and spark-ml, latests versions. 

## Things to do:

 - Saving to mongo instance the results from ALS algorithm... DONE!
 - Rewrite the whole project using the microservice RESTful style 
 - upgrade library versions
 - docker!
 - add more machine learning algorithms to improve the recommendations. Adding more flows to the machine learning workflow.


have fun in the process!

[1]: https://dl.acm.org/citation.cfm?id=1608614
[2]: https://spark.apache.org/docs/2.2.0/ml-collaborative-filtering.html

Thank you @emecas.
