package com.samebug.accesslog

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Top1000HostsJob {
  def main(args: Array[String]) {
    Top1000Hosts.execute(master = None, args = args, jars = Seq(SparkContext.jarOfObject(this).get))
  }
}


object Top1000Hosts {
  def execute(master: Option[String], args: Seq[String], jars: Seq[String] = Nil) {
    val sc = initContext(master, jars)
    try {
      val file: RDD[String] = sc.textFile(args.head)
      val records: RDD[CombinedLogRecord] = file.collect(CombinedLogRecord.parseRecord)

      top1000Hosts(file.count(), records)
    } finally {
      sc.stop()
    }
  }

  def top1000Hosts(numberOfLines: Long, records: RDD[CombinedLogRecord]): Unit = {
    val clients: RDD[(String, CombinedLogRecord)] = records map { r => r.clientIp -> r }
    val numberOfPageViewsPerClient: RDD[(String, Int)] = clients.aggregateByKey(0)({case(pageViews, _) =>  pageViews + 1}, { _ + _ })
    val clientsSortedByPageViewsDescending = numberOfPageViewsPerClient.sortBy(-_._2)
    clientsSortedByPageViewsDescending.saveAsTextFile("clients.txt")
    val top1000 = clientsSortedByPageViewsDescending.take(100)
    val top1000Hosts = top1000 map { case (ip, pageViews) => hostName(ip) -> pageViews }
    println(s"Top ${top1000Hosts.size } clients collected from ${records.count() } records, $numberOfLines log rows")
    top1000Hosts foreach { case (host, visits) => println(s"$visits\t$host") }
  }

  private def hostName(ip: String) = java.net.InetAddress.getByName(ip).getCanonicalHostName

  private def initContext(master: Option[String], jars: Seq[String]): SparkContext = {
    val conf = new SparkConf().setAppName("Sample Analysis").setJars(jars)
    master foreach conf.setMaster
    new SparkContext(conf)
  }
}
