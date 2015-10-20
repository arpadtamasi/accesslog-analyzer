package com.samebug.accesslog

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.matching.Regex

object FindFalseNegativesJob {
  def main(args: Array[String]) {
    FindFalseNegatives.execute(master = None, args = args, jars = Seq(SparkContext.jarOfObject(this).get))
  }
}


object FindFalseNegatives {
  def execute(master: Option[String], args: Seq[String], jars: Seq[String] = Nil) {
    val sc = initContext(master, jars)
    try {
      val file: RDD[String] = sc.textFile(args.head)
      val dropLines: RDD[(String, String)] = file collect {
        case DropAddressPattern(url, cause) => (url, cause)
      }
     val causes: RDD[(String, Iterable[(String, String)])] = dropLines.groupBy(_._2)


      causes map { case(cause, lines) => cause -> lines.size} foreach println
    } finally {
      sc.stop()
    }
  }

  private val DropAddressPattern: Regex = "(.*) dropped as (.*)".r
  private val BanLinePattern = " mustHave/banned were ([\\d\\.]*)".r

  private def initContext(master: Option[String], jars: Seq[String]): SparkContext = {
    val conf = new SparkConf().setAppName("Sample Analysis").setJars(jars)
    master foreach conf.setMaster
    new SparkContext(conf)
  }
}
