package pricemigrationengine.model

sealed trait Failure {
  val reason: String
}

case class ConfigurationFailure(reason: String) extends Failure

case class CohortFetchFailure(reason: String) extends Failure
case class CohortUpdateFailure(reason: String) extends Failure

case class ZuoraFetchFailure(reason: String) extends Failure
case class ZuoraUpdateFailure(reason: String) extends Failure

case class AmendmentDataFailure(reason: String) extends Failure
case class CancelledSubscriptionFailure(reason: String) extends Failure

case class SalesforceFailure(reason: String) extends Failure
case class SalesforcePriceRiseCreationFailure(reason: String) extends Failure
case class SalesforceClientFailure(reason: String) extends Failure

case class S3Failure(reason: String) extends Failure
case class SubscriptionIdUploadFailure(reason: String) extends Failure