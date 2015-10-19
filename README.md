Access log analyzis using Spark
===============================

Requirements
------------

-   Apache Spark
    [Download](<http://www.apache.org/dyn/closer.lua/spark/spark-1.5.1/spark-1.5.1.tgz>)

-   Scala

-   Sbt

-   Git

Clone
-----

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
git clone https://github.com/progos/accesslog-analyzer.git
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Build
-----

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
sbt assembly
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Run
---

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
export SPARK_MASTER="spark://192.168.1.123:7077"
export HDFS_ROOT="hdfs://192.168.1.123:9000"
spark-submit --class "com.samebug.accesslog.Top1000HostsJob" --master $SPARK_MASTER target/scala-2.10/accesslog-analyzer.jar "$HDFS_ROOT/accesslogs/access.log*"
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Enjoy
-----
