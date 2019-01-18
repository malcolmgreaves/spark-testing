package com.nitro.test

import org.sparktest.test.SparkTesting

import scala.util.Random

class UniformSparkTest extends SparkTesting {

  sparkTest("Correctly estimate mean from Uniform(0,1) data.") { sc =>
    val n = 5000000
    val randData = sc.parallelize(Seq.fill(n)(Random.nextDouble()))
    val mean = randData.sum / n.toDouble
    assert(math.abs(mean - 0.5) < 0.001)
  }
}