	The purpose of this project is to learn how to code a recommendation engine using kafka, spark, mongodb or cassandra, websockets and play.

	The idea of the project comes from the big data course that i received from formacionhadoop.com

	These are the components:
	
	A kafka producer is going to ask periodically to Amazon in order to know what products based on my own ratings 
	and i am going to introduced them into some kafka topic.
	
	A spark streaming process is going to read from that previous topic.
	
	Apply some machine learning algorithms (ALS, content based filtering colaborative filtering) on those datasets readed by 
	the spark streaming process.

	Save results in a mongo or cassandra instance.

	Use play framework to create an websocket interface between the mongo instance and the visual interface.

	I am going to use some ideas from a previous work: 

	https://github.com/alonsoir/hello-kafka-twitter-scala

	https://github.com/alonsoir/recomendation-spark-engine

	Actually the project can push data to kafka topic, the spark streaming process can recover data from the topic and
	save them into mongo instance.

	Actual output:

	MacBook-Pro-Retina-de-Alonso:bin aironman$ ./amazon-kafka-connector 127.0.0.1:9092 amazonRatingsTopic
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

	tested a mongodb connection with stratio library...
	finished withSQLContext...
	16/05/16 18:48:53 INFO BlockManagerInfo: Removed broadcast_0_piece0 on localhost:57536 in memory (size: 2.5 KB, free: 2.4 GB)
	...
	16/05/16 18:48:57 INFO StreamingContext: Invoking stop(stopGracefully=false) from shutdown hook
	16/05/16 18:48:57 INFO JobGenerator: Stopping JobGenerator immediately
	16/05/16 18:48:57 INFO RecurringTimer: Stopped timer for JobGenerator after time 1463417336000
	16/05/16 18:48:57 INFO JobGenerator: Stopped JobGenerator
	16/05/16 18:48:57 INFO JobScheduler: Stopped JobScheduler
	Finished!


	MacBook-Pro-Retina-de-Alonso:bin aironman$ ./amazon-producer-example 0981531679
	log4j:WARN No appenders could be found for logger (kafka.utils.VerifiableProperties).
	log4j:WARN Please initialize the log4j system properly.
	log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
	Trying to parse product with id 0981531679
	amazonProduct is AmazonProduct(0981531679,Scala Puzzlers,http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679,http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg,)
	amazon product sent to kafka cluster...AmazonProduct(0981531679,Scala Puzzlers,http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679,http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg,)

	Last login: Mon May 16 18:31:09 on ttys003
	MacBook-Pro-Retina-de-Alonso:my-recommendation-spark-engine aironman$ mongo
	MongoDB shell version: 3.2.6
	connecting to: test
	> use alonsodb;
	switched to db alonsodb
	> db.amazonRatings.find()
	{ "_id" : ObjectId("5739f84a8d6ab41037bbf32d"), "id" : ISODate("2016-05-16T16:41:46.183Z"), "amazonProduct" : [ null, "{\"itemId\":\"0981531679\",\"title\":\"Scala Puzzlers\",\"url\":\"http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679\",\"img\":\"http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg\",\"description\":\"\"}" ] }
	{ "_id" : ObjectId("5739f8628d6ab41037bbf32e"), "id" : ISODate("2016-05-16T16:42:10.025Z"), "amazonProduct" : [ null, "{\"itemId\":\"0981531679\",\"title\":\"Scala Puzzlers\",\"url\":\"http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679\",\"img\":\"http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg\",\"description\":\"\"}" ] }
	{ "_id" : ObjectId("5739f9308d6ab41037bbf32f"), "id" : ISODate("2016-05-16T16:45:36.021Z"), "amazonProduct" : [ null, "{\"itemId\":\"0981531679\",\"title\":\"Scala Puzzlers\",\"url\":\"http://www.amazon.com/Scala-Puzzlers-Andrew-Phillips/dp/0981531679\",\"img\":\"http://ecx.images-amazon.com/images/I/41UHeor2AfL._SX218_BO1,204,203,200_QL40_.jpg\",\"description\":\"\"}" ] }
	> 


	Things to do:
	
	Saving to mongo instance the results from ALS algorithm...
	have fun in the process!
