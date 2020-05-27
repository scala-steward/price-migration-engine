package pricemigrationengine.handlers

import java.io.InputStream

import pricemigrationengine.model._
import pricemigrationengine.services._
import zio.Exit.Success
import zio.Runtime.default
import zio._
import zio.stream.ZStream

import scala.collection.mutable.ArrayBuffer

class SubscriptionIdUploadHandlerTest extends munit.FunSuite {
  test("SubscriptionIdUploadHandler should get subscriptions from s3 and write to cohort table") {
    val stubConfiguration = ZLayer.succeed(
      new StageConfiguration.Service {
        override val config: IO[ConfigurationFailure, StageConfig] =
          IO.succeed(StageConfig("DEV"))
      }
    )

    val subscriptionsWrittenToCohortTable = ArrayBuffer[Subscription]()

    val stubCohortTable = ZLayer.succeed(
      new CohortTable.Service {
        override def fetch(filter: CohortTableFilter): IO[CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]] = ???
        override def update(result: EstimationResult): ZIO[Any, CohortUpdateFailure, Unit] = ???
        override def update(subscriptionName: String, result: SalesforcePriceRiseCreationDetails): ZIO[Any, CohortUpdateFailure, Unit] = ???
        override def update(result: AmendmentResult): ZIO[Any, CohortUpdateFailure, Unit] = ???
        override def put(subscription: Subscription): ZIO[Any, CohortUpdateFailure, Unit] =
          IO.effect {
            subscriptionsWrittenToCohortTable.addOne(subscription)
            ()
          }.mapError(_ => CohortUpdateFailure(""))
      }
    )

    val stubS3: Layer[Nothing, Has[S3.Service]] = ZLayer.succeed(
      new S3.Service {
        def loadTestResource(path: String) = {
          ZManaged.makeEffect(getClass.getResourceAsStream(path)) { stream =>
            stream.close()
          }.mapError(ex => S3Failure(s"Failed to load test resource: $ex"))
        }

        override def getObject(s3Location: S3Location): ZManaged[Any, S3Failure, InputStream] = s3Location match {
          case S3Location("price-migration-engine-dev", "excluded-subscription-ids.csv") =>
            loadTestResource("/SubscriptionExclusions.csv")
          case S3Location("price-migration-engine-dev", "salesforce-subscription-id-report.csv") =>
            loadTestResource("/SubscriptionIds.csv")
        }
      }
    )

    val stubLogging = console.Console.live >>> ConsoleLogging.impl

    assertEquals(
      default.unsafeRunSync(
        SubscriptionIdUploadHandler.main
          .provideLayer(
            stubLogging ++ stubConfiguration ++ stubCohortTable ++ stubS3
          )
      ),
      Success(())
    )
    assertEquals(subscriptionsWrittenToCohortTable.size, 2)
    assertEquals(subscriptionsWrittenToCohortTable(0).subscriptionNumber, "A-S123456")
    assertEquals(subscriptionsWrittenToCohortTable(1).subscriptionNumber, "654321")
  }
}
