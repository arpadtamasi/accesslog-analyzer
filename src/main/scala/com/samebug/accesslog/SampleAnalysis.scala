package com.samebug.accesslog

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SampleAnalysis {

  def seqOp(accumulator: Map[String, Int], userAgent: String): Map[String, Int] = accumulator.get(userAgent) match {
    case None => accumulator + (userAgent -> 1)
    case Some(count) => accumulator + (userAgent -> (count + 1))
  }

  def combOp(acc1: Map[String, Int], acc2: Map[String, Int]): Map[String, Int] =
    acc1 ++ acc2.map { case (userAgent, count) =>
      userAgent -> (count + acc1.getOrElse(userAgent, 0))
    }


  def execute(master: Option[String], args: List[String], jars: Seq[String] = Nil) {
    val sc = initContext(master, jars)
    val domainlookup = new DomainLookup("lookup.txt")
    val records: RDD[CombinedLogRecord] = sc.textFile(args(0)) map CombinedLogRecord.parseRecord.lift filter { _.nonEmpty } map { _.get }
    val clients: RDD[(String, String)] = records map { r => r.clientIp -> r.userAgent }
    val clientsWithAgents: RDD[(String, Map[String, Int])] = clients.aggregateByKey(Map.empty[String, Int])(seqOp, combOp)
    val clientsWithTraffic: RDD[(String, (Int, Map[String, Int]))] = clientsWithAgents map { case (client, agents) => client ->(agents.values.sum, agents) }
    val clientsWithHighTraffic = clientsWithTraffic filter { case (_, (pageViews, _)) => pageViews > 100 }
    val clientsWithHosts: RDD[(String, (String, Int, Map[String, Int]))] = clientsWithHighTraffic map { case (ip, (pageViews, agents)) => ip ->(domainlookup.host(ip), pageViews, agents) }

    clientsWithHosts foreach { case (ip, (host, pageViews, agents)) =>
      println(s"$ip\t\host\t$pageViews")
      agents foreach { case (agent, pageViews) =>
        println(s"\t$pageViews\t$agent")
      }
    }
  }

  private def initContext(master: Option[String], jars: Seq[String]): SparkContext = {
    val conf = new SparkConf().setAppName("Sample Analysis").setJars(jars)
    master foreach conf.setMaster
    new SparkContext(conf)
  }
}
