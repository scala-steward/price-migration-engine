import sbt._

object Dependencies {
  private val zioVersion = "1.0.4"
  private val awsSdkVersion = "1.11.940"
  private val upickleVersion = "1.2.2"

  lazy val awsCore = "com.amazonaws" % "aws-java-sdk-core" % awsSdkVersion
  lazy val awsDynamoDb = "com.amazonaws" % "aws-java-sdk-dynamodb" % awsSdkVersion
  lazy val awsS3 = "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion
  lazy val awsSQS = "com.amazonaws" % "aws-java-sdk-sqs" % awsSdkVersion
  lazy val awsStateMachine = "com.amazonaws" % "aws-java-sdk-stepfunctions" % awsSdkVersion
  lazy val zio = "dev.zio" %% "zio" % zioVersion
  lazy val zioStreams = "dev.zio" %% "zio-streams" % zioVersion
  lazy val zioTest = "dev.zio" %% "zio-test" % zioVersion
  lazy val zioTestSbt = "dev.zio" %% "zio-test-sbt" % zioVersion
  lazy val izumiReflect = "dev.zio" %% "izumi-reflect" % "1.0.0-M12"
  lazy val scalaCompat = "org.scala-lang.modules" %% "scala-collection-compat" % "2.3.2"
  lazy val geny = "com.lihaoyi" %% "geny" % "0.6.4"
  lazy val ujson = "com.lihaoyi" %% "ujson" % upickleVersion
  lazy val upickleCore = "com.lihaoyi" %% "upickle-core" % upickleVersion
  lazy val upickle = "com.lihaoyi" %% "upickle" % upickleVersion
  lazy val upickleImplicits = "com.lihaoyi" %% "upickle-implicits" % upickleVersion
  lazy val awsLambda = "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
  lazy val http = "org.scalaj" %% "scalaj-http" % "2.4.2"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.20"
  lazy val commonsCsv = "org.apache.commons" % "commons-csv" % "1.8"
}
