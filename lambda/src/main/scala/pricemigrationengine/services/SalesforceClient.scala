package pricemigrationengine.services

import pricemigrationengine.model.{SalesforceClientFailure, SalesforcePriceRise, SalesforceSubscription}
import zio.{IO, ZIO}

case class SalesforcePriceRiseCreationResponse(id: String)

object SalesforceClient {
  trait Service {
    def getSubscriptionByName(subscrptionName: String): IO[SalesforceClientFailure, SalesforceSubscription]
    def createPriceRise(priceRise: SalesforcePriceRise): IO[SalesforceClientFailure, SalesforcePriceRiseCreationResponse]
    def updatePriceRise(priceRiseId: String, priceRise: SalesforcePriceRise): IO[SalesforceClientFailure, Unit]
  }

  def getSubscriptionByName(
    subscrptionName: String
  ): ZIO[SalesforceClient, SalesforceClientFailure, SalesforceSubscription] =
    ZIO.accessM(_.get.getSubscriptionByName(subscrptionName))

  def createPriceRise(
    priceRise: SalesforcePriceRise
  ): ZIO[SalesforceClient, SalesforceClientFailure, SalesforcePriceRiseCreationResponse] =
    ZIO.accessM(_.get.createPriceRise(priceRise))

  def updatePriceRise(
    priceRiseId: String,
    priceRise: SalesforcePriceRise
  ): ZIO[SalesforceClient, SalesforceClientFailure, Unit] =
    ZIO.accessM(_.get.updatePriceRise(priceRiseId, priceRise))
}