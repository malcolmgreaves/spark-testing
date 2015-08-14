package com.nitro.test

import scala.util.Random

class GaussianSparkTest extends SparkTesting {

  sparkTest("Correctly estimate mean from Gaussian(0,1) data.") { sc =>
    val n = 5000000
    val randData = sc.parallelize(Seq.fill(n)(Random.nextGaussian()))
    val mean = randData.sum / n.toDouble
    assert(math.abs(mean - 0.0) < 0.001)
  }
}