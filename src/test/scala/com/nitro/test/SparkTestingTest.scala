package com.nitro.test

import org.apache.spark.{ SparkConf, SparkContext }
import org.scalatest.FunSuite

class SparkTestingTest extends SparkTesting {
  sparkTest("Hello world!") {
    val wordSet = "hello world how are you doing today".split(" ").toSet
    val words = sc.parallelize(wordSet.toSeq)
    val wordSetFromRdd =
      words
        .aggregate(Set.empty[String])(
          { case (s, word) => s + word },
          { case (s1, s2) => s1 union s2 }
        )
    assert(wordSet == wordSetFromRdd)
  }
}
