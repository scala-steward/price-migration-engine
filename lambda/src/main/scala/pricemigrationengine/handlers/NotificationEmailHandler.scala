package pricemigrationengine.handlers

import com.amazonaws.services.lambda.runtime.Context
import pricemigrationengine.model.Failure
import pricemigrationengine.services._
import zio.console.Console
import zio.random.Random
import zio.{Runtime, ZEnv, ZIO, ZLayer, console}

object NotificationEmailHandler {
  val main: ZIO[Logging with CohortTable with SalesforceClient, Failure, Unit] = ???

  private def env(
    loggingLayer: ZLayer[Any, Nothing, Logging]
  ): ZLayer[Any, Any, Logging with CohortTable with SalesforceClient] = {
    val cohortTableLayer =
      loggingLayer ++ EnvConfiguration.dynamoDbImpl >>>
        DynamoDBClient.dynamoDB ++ loggingLayer >>>
        DynamoDBZIOLive.impl ++ loggingLayer ++ EnvConfiguration.cohortTableImp ++
          EnvConfiguration.stageImp ++ EnvConfiguration.salesforceImp >>>
        CohortTableLive.impl ++ SalesforceClientLive.impl
    loggingLayer ++ EnvConfiguration.amendmentImpl ++ cohortTableLayer
  }

  private val runtime = Runtime.default

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    main
      .provideSomeLayer(
        env(Console.live >>> ConsoleLogging.impl)
      )
      .foldM(
        e => console.putStrLn(s"Failed: $e") *> ZIO.succeed(1),
        _ => console.putStrLn("Succeeded!") *> ZIO.succeed(0)
      )

  def handleRequest(unused: Unit, context: Context): Unit =
    runtime.unsafeRun(
      main.provideSomeLayer(
        env(LambdaLogging.impl(context))
      )
    )
}