package com.nitro.test

import java.util.concurrent.locks.ReentrantReadWriteLock

import org.apache.spark.{ SparkConf, SparkContext }
import org.scalatest.FunSuite

object SparkTesting extends org.scalatest.Tag("spark") {

  private[this] val l = new ReentrantReadWriteLock()

  def lock(): Unit =
    l.writeLock().lock()

  def unlock(): Unit =
    l.writeLock().unlock()

}

trait SparkTesting extends FunSuite {

  var sc: SparkContext = _

  /**
   * convenience method for tests that use spark.  Creates a local spark context, and cleans
   * it up even if your test fails.  Also marks the test with the tag SparkTesting, so you can
   * turn it off
   *
   * By default, it turn off spark logging, b/c it just clutters up the test output.  However,
   * when you are actively debugging one test, you may want to turn the logs on
   *
   * @param name the name of the test
   */
  def sparkTest(name: String)(body: => Unit) =
    try {

      SparkTesting.lock()

      test(name, SparkTesting) {

        sc = new SparkContext(
          new SparkConf()
            .setMaster("local[2]")
            .setAppName(name)
            .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        )

        try {

          body

        } finally {
          sc.stop()
          sc = null
          // To avoid Akka rebinding to the same port, since it doesn't unbind immediately on shutdown.
          System.clearProperty("spark.master.port")
          System.gc()
        }
      }

    } finally {

      SparkTesting.unlock()

    }

  def ignoreSparkTest(name: String, logIgnored: Boolean = true)(bodyIgnored: => Unit) =
    ignore(name, SparkTesting) { /* test is ignored, so doesn't matter what we do! */ }

}
