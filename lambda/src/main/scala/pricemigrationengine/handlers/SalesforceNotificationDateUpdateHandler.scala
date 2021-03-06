package pricemigrationengine.handlers

import java.time.{LocalDate, ZoneOffset}

import pricemigrationengine.model.CohortTableFilter.{NotificationSendComplete, NotificationSendDateWrittenToSalesforce}
import pricemigrationengine.model._
import pricemigrationengine.services._
import zio.clock.Clock
import zio.{IO, ZEnv, ZIO, ZLayer}

object SalesforceNotificationDateUpdateHandler extends CohortHandler {

  val main: ZIO[Logging with CohortTable with SalesforceClient with Clock, Failure, HandlerOutput] =
    for {
      cohortItems <- CohortTable.fetch(NotificationSendComplete, None)
      _ <- cohortItems.foreach(updateDateLetterSentInSF)
    } yield HandlerOutput(isComplete = true)

  private def updateDateLetterSentInSF(
      item: CohortItem
  ): ZIO[Logging with CohortTable with SalesforceClient with Clock, Failure, Unit] =
    for {
      _ <- updateSalesforce(item)
        .tapBoth(
          e => Logging.error(s"Failed to write create Price_Rise in salesforce: $e"),
          result => Logging.info(s"SalesforcePriceRise result: $result")
        )
      now <- Time.thisInstant
      salesforcePriceRiseDetails = CohortItem(
        subscriptionName = item.subscriptionName,
        processingStage = NotificationSendDateWrittenToSalesforce,
        whenNotificationSentWrittenToSalesforce = Some(now)
      )
      _ <-
        CohortTable
          .update(salesforcePriceRiseDetails)
    } yield ()

  private def updateSalesforce(
      cohortItem: CohortItem
  ): ZIO[SalesforceClient, Failure, Option[String]] = {
    for {
      priceRise <- buildPriceRise(cohortItem)
      salesforcePriceRiseId <-
        IO
          .fromOption(cohortItem.salesforcePriceRiseId)
          .orElseFail(
            SalesforcePriceRiseWriteFailure(
              "CohortItem.salesforcePriceRiseId is required to update salesforce"
            )
          )
      result <-
        SalesforceClient
          .updatePriceRise(salesforcePriceRiseId, priceRise)
          .as(None)
    } yield result
  }

  def buildPriceRise(
      cohortItem: CohortItem
  ): IO[SalesforcePriceRiseWriteFailure, SalesforcePriceRise] = {
    for {
      notificationSendTimestamp <-
        ZIO
          .fromOption(cohortItem.whenNotificationSent)
          .orElseFail(SalesforcePriceRiseWriteFailure(s"$cohortItem does not have a whenEmailSent field"))
    } yield SalesforcePriceRise(
      Date_Letter_Sent__c = Some(LocalDate.from(notificationSendTimestamp.atOffset(ZoneOffset.UTC)))
    )
  }

  private def env(cohortSpec: CohortSpec): ZLayer[Logging, Failure, CohortTable with SalesforceClient with Logging] =
    (LiveLayer.cohortTable(cohortSpec) and LiveLayer.salesforce and LiveLayer.logging)
      .tapError(e => Logging.error(s"Failed to create service environment: $e"))

  def handle(input: CohortSpec): ZIO[ZEnv with Logging, Failure, HandlerOutput] =
    main.provideSomeLayer[ZEnv with Logging](env(input))
}
