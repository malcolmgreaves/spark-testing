package org.sparktest.test

class HelloSparkTest extends SparkTesting {

  sparkTest("Hello world!") { sc =>
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