The purpose of this project is to learn the basics about how to code a almost near real time 
rating based recommendation engine. The simple idea is to calculate recomendations of 
different items using notes that other users have given to other products, 
recalculating as quickly as possible, that is, as soon as the note has arrived in the system.

These are the components:
	
A kafka producer is going to ask periodically to Amazon in order to know what products based on 
my own ratings and i am going to introduced them into some kafka topic.
	
A spark streaming process is going to read from that previous topic.
	
Apply some machine learning algorithms (ALS, content based filtering colaborative filtering) on those datasets readed by 
the spark streaming process and save the result to somewhere, a mongo instance.


I am going to use some ideas from a previous work: 

	https://github.com/alonsoir/hello-kafka-twitter-scala

	https://github.com/alonsoir/recomendation-spark-engine

Actually the project can push data to kafka topic, the spark streaming process can recover data from the topic and
save them into mongo instance.

Actual output:

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
