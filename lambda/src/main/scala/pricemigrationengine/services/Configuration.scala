package pricemigrationengine.services

import pricemigrationengine.model._
import zio.{IO, ZIO}

object ZuoraConfiguration {
  trait Service {
    val config: IO[ConfigurationFailure, ZuoraConfig]
  }

  val zuoraConfig: ZIO[ZuoraConfiguration, ConfigurationFailure, ZuoraConfig] =
    ZIO.accessM(_.get.config)
}

object CohortTableConfiguration {
  trait Service {
    val config: IO[ConfigurationFailure, CohortTableConfig]
  }

  val cohortTableConfig: ZIO[CohortTableConfiguration, ConfigurationFailure, CohortTableConfig] =
    ZIO.accessM(_.get.config)
}

object SalesforceConfiguration {
  trait Service {
    val config: IO[ConfigurationFailure, SalesforceConfig]
  }

  val salesforceConfig: ZIO[SalesforceConfiguration, ConfigurationFailure, SalesforceConfig] =
    ZIO.accessM(_.get.config)
}

object StageConfiguration {
  trait Service {
    val config: IO[ConfigurationFailure, StageConfig]
  }

  val stageConfig: ZIO[StageConfiguration, ConfigurationFailure, StageConfig] =
    ZIO.accessM(_.get.config)
}

object EmailSenderConfiguration {
  trait Service {
    val config: IO[ConfigurationFailure, EmailSenderConfig]
  }

  val emailSenderConfig: ZIO[EmailSenderConfiguration, ConfigurationFailure, EmailSenderConfig] =
    ZIO.accessM(_.get.config)
}

object CohortStateMachineConfiguration {
  trait Service {
    val config: IO[ConfigurationFailure, CohortStateMachineConfig]
  }
  val cohortStateMachineConfig: ZIO[CohortStateMachineConfiguration, ConfigurationFailure, CohortStateMachineConfig] =
    ZIO.accessM(_.get.config)
}

object ExportConfiguration {
  trait Service {
    val config: ExportConfig
  }
  val exportConfig: ZIO[ExportConfiguration, ConfigurationFailure, ExportConfig] =
    ZIO.access(_.get.config)
}
