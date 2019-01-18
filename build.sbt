organization := "io.malcolmgreaves"
name := "spark-testing"
version := "0.0.2"

// dependencies & their resolvers
resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.2.2",
  "org.scalatest" %% "scalatest" % "3.0.5"
)


// scala compilation settings
lazy val javaV = "1.8"
scalaVersion in ThisBuild := "2.11.12"
scalacOptions in ThisBuild := Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  s"-target:jvm-$javaV",
  "-encoding",
  "utf8",
  "-language:postfixOps",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-language:reflectiveCalls",
  "-Yno-adapted-args",
  "-Ywarn-value-discard",
  "-Xlint",
  "-Xfuture",
  "-Ywarn-dead-code",
  "-Xfatal-warnings" // Every warning is esclated to an error.
)
// java compilation settings
javacOptions in ThisBuild := Seq("-source", javaV, "-target", javaV)
javaOptions in ThisBuild := Seq(
  "-server",
  "-XX:+AggressiveOpts",
  "-XX:+TieredCompilation",
  "-XX:CompileThreshold=100",
  "-Xmx3000M",
  "-XX:+UseG1GC"
)

// test, runtime settings
//
fork in run := false
testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
testOptions in Test += Tests.Argument("-oF")
fork in Test := false
parallelExecution in Test := true

// publish settings
//
publishMavenStyle := true
isSnapshot := false
publishArtifact := true
publishArtifact in Test := false
publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
pomIncludeRepository := { _ => false }
pomExtra := {
  <url>https://github.com/malcolmgreaves/spark-testing</url>
    <licenses>
    </licenses>
    <scm>
      <url>git@ggithub.com:malcolmgreaves/spark-testing</url>
      <connection>scm:git@github.com:malcolmgreaves/spark-testing.git</connection>
    </scm>
    <developers>
      <developer>
        <id>malcolmgreaves</id>
        <name>Malcolm Greaves</name>
        <email>greaves.malcolm@gmail.com</email>
        <url>https://malcolmgreaves.io/</url>
      </developer>
    </developers>
}
