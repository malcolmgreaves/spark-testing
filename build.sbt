organization := "com.gonitro.research"

name := "spark-testing"

version := "0.0.1"

// bring the sbt-dev-settings stuff into scope

import com.nitro.build._

import PublishHelpers._

// use it to configure this build's scala and java settings

lazy val jvmVer = JvmRuntime.Jvm7

CompileScalaJava.librarySettings(CompileScalaJava.Config.spark)

javaOptions := JvmRuntime.settings(jvmVer)

// standard dependencies & their resolvers

resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Nitro Nexus Releases" at "https://nexus.nitroplatform.com/nexus/content/repositories/releases/"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.4.0",
  "org.scalatest"    %% "scalatest"  % "2.2.4" 
)

// publishing settings

Publish.settings(
  repo = Repository.github("Nitro", name.toString),
  developers = 
    Seq(
      Dev("mgreaves", "Malcolm Greaves")
    ),
  art = ArtifactInfo.sonatype,
  lic = License.apache20
)

// unit test configuration

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")

testOptions in Test += Tests.Argument("-oF")

fork in Test := false

parallelExecution in Test := true

