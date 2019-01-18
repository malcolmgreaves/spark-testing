sparktesting
============
[![Build Status](https://travis-ci.org/malcolmgreaves/spark-testing.svg?branch=master)](https://travis-ci.org/malcolmgreaves/spark-testing)

A tiny project that assists in writing Apache Spark based unit tests by 
providing a single shared `SparkContext`.


# For Developers

Use `sbt` to `compile`, `test`, and view `coverage` reports 
(with `sbt coverage test coverageReport`).

# For Users

### Proper `build.sbt` Configuration

Ensure that `sbt` does _not_ fork and run tests in parallel when using this library.
I.e. ensure that the following setting is present in your `build.sbt`:
```sbt
fork in Test := false
```

### Using a Shared Spark Context for Tests

To make a scalatest class that uses a single, shared `SparkContext`, extend from the
`SparkTesting` class. A stub that shows this extension is defined below:
```scala
import org.sparktest.test.SparkTesting

class CustomSparkTest extends SparkTesting {

  sparkTest("A test that uses a shared SparkContext") { sc =>
    // sc: org.apache.spark.SparkContext
    // your custom testing code that uses Spark goes here! :D
    ???
  }
}
```

