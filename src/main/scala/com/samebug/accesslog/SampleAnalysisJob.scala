package com.samebug.accesslog


import org.apache.spark.SparkContext

object SampleAnalysisJob {

  def main(args: Array[String]) {

    SampleAnalysis.execute(
      master = None,
      args = args.toList,
      jars = List(SparkContext.jarOfObject(this).get)
    )

    System.exit(0)
  }
}
