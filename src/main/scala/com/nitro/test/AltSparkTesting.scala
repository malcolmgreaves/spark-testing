package com.nitro.test

import java.util.concurrent.Semaphore

import org.scalatest.{ Tag, BeforeAndAfterAll, FunSuite }

import scala.util.{ Try, Random }
import java.util.concurrent.locks.ReentrantReadWriteLock

import org.apache.spark.{ SparkConf, SparkContext }

trait Lockable {

  private[this] val l = new ReentrantReadWriteLock()

  def lock(): Unit =
    l.writeLock().lock()

  def unlock(): Unit =
    l.writeLock().unlock()
}

trait WaitGroup {

  private[this] val lk = new Lockable {}

  private[this] var counter = 0

  def add(k: Int): Unit = {
    lk.lock()
    try {
      counter += 1
    } finally {
      lk.unlock()
    }
  }

  def done(k: Int): Unit = {
    lk.lock()
    try {
      counter -= k
      if (counter < 0) {
        throw new IllegalStateException("Can never call add more than done!")
      }

    } finally {
      lk.unlock()
    }
  }

  def waitUntilCompletion(): Unit =
    while (!counterIsZero) {
      Thread.sleep(1300)
    }

  private[this] def counterIsZero: Boolean = {
    lk.lock()
    try {
      counter == 0
    } finally {
      lk.unlock()
    }
  }

}

object AltSparkTesting extends org.scalatest.Tag("spark") {

  //
  //
  // Executing a function that requires use of the internal SparkContext.
  //
  //

  private[this] val wg = new WaitGroup {}

  def beforeSuite(): Unit =
    wg.add(1)

  def afterSuite(): Unit =
    wg.done(1)

  def awaitAllSparkTests(): Unit = {
    wg.waitUntilCompletion()
    shutdown()
  }

  def runWithSpark(f: SparkContext => Any): Unit =
    runWithSpark(f, defaultSparkConf)

  def runWithSpark(f: SparkContext => Any, configIfInit: => SparkConf): Unit = {

    // safely grab shared spark context
    val usingSparkContext = sc(configIfInit)

    // execute the test
    wg.add(1)
    try {
      f(usingSparkContext)
      //Wwe don't catch exceptions as we want to propigate exceptional cases
      // to callers. We do, however, want to ensure that we properly decrement
      // from the internal wait group.
      ()

    } finally {
      wg.done(1)
    }
  }

  //
  //
  // Access to internal SparkContext.
  //
  //

  private[this] val mutex = new Lockable {}

  /**
   * WARNING :: Mutable! Global!
   *
   * Do not access this variable in any other manner except
   * through the `sc` method. This is to ensure that there
   * are no race conditions nor two spark contexts in the
   * same JVM at one time.
   */
  private[this] var scInternal: SparkContext = _

  lazy val defaultSparkConf: SparkConf =
    new SparkConf()
      .setMaster("local[3]")
      .setAppName(s"unit test ${Random.nextString(3)}-${Random.nextString(4)}")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

  /**
   * Blocking.
   *
   * @return
   */
  private[test] def sc(configIfInit: => SparkConf = defaultSparkConf): SparkContext =
    try {
      mutex.lock()
      if (scInternal == null) {
        scInternal = new SparkContext(configIfInit)
      }
      scInternal

    } finally {
      mutex.unlock()
    }

  private[test] def shutdown(): Unit =
    try {
      mutex.lock()
      if (scInternal != null) {
        scInternal.stop()
        scInternal = null
        // To avoid Akka rebinding to the same port,
        // since it doesn't unbind immediately on shutdown.
        System.clearProperty("spark.master.port")
        System.gc()
      }
      ()

    } finally {
      mutex.unlock()
    }
}

trait AltSparkTesting extends FunSuite with BeforeAndAfterAll {

  final override def beforeAll(): Unit =
    AltSparkTesting.beforeSuite()

  final override def afterAll(): Unit = {
    AltSparkTesting.afterSuite()
    AltSparkTesting.awaitAllSparkTests()
  }

  /**
   * Calls the parent `test` method with the same parameters, except
   * that there is only one tag that is used: `AltSparkTesting`.
   */
  final override def test(testName: String, ignored: Tag*)(testFun: => Unit): Unit =
    super.test(testName, AltSparkTesting)(testFun)

  /**
   * Calls the parent `ignore` method with the same parameters, except
   * that there is only one tag that is used: `AltSparkTesting`.
   */
  final override def ignore(testName: String, ignored: Tag*)(testFun: => Unit): Unit =
    super.ignore(testName, AltSparkTesting)(testFun)

  /**
   * Runs as a FunSuite test: calls this overriden test method. Uses a shared
   * SparkContext to run the supplied test function `testFun`.
   */
  final def sparkTest(testName: String)(testFun: SparkContext => Any): Unit =
    test(testName) {
      AltSparkTesting.runWithSpark(testFun)
    }

  /**
   * An ignored Spark-enabled unit test. Useful when one must temporarily
   * disable a suite's test during gnarly debugging or development.
   *
   * NOTE:
   *
   * A stylistic break in naming convention is used in this method. Since its
   * use is in test code, this method name should never see the light of day
   * in any publicly facing APIs (or even internal ones). The concession in
   * naming convention is done out of the following practical observation:
   *
   * It is easier to prepend "ignore_" to an existing spark test definition than it
   * is to prepend "ignore", delete the "s", and then type in an "S" to keep
   * camel case naming convention. ("ignore_sparkTest" vs. "ignoreSparkTest")
   */
  final def ignore_sparkTest(testName: String)(ignoredTestFun: SparkContext => Any): Unit =
    ignore(testName) { () }

}

