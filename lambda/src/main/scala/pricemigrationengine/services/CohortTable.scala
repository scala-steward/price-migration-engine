package pricemigrationengine.services

import java.time.LocalDate

import pricemigrationengine.model._
import zio.stream.ZStream
import zio.{IO, ZIO}

case class CohortTableKey(subscriptionNumber: String)

object CohortTable {
  trait Service {
    def fetch(
        filter: CohortTableFilter,
        beforeDateInclusive: Option[LocalDate]
    ): IO[CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]]

    def fetchAll(): IO[CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]]

    def create(cohortItem: CohortItem): ZIO[Any, Failure, Unit]

    def update(result: CohortItem): ZIO[Any, CohortUpdateFailure, Unit]
  }

  def fetch(
      filter: CohortTableFilter,
      beforeDateInclusive: Option[LocalDate]
  ): ZIO[CohortTable, CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]] =
    ZIO.accessM(_.get.fetch(filter, beforeDateInclusive))

  def fetchAll(): ZIO[CohortTable, CohortFetchFailure, ZStream[Any, CohortFetchFailure, CohortItem]] =
    ZIO.accessM(_.get.fetchAll())

  def create(subscription: CohortItem): ZIO[CohortTable, Failure, Unit] =
    ZIO.accessM(_.get.create(subscription))

  def update(result: CohortItem): ZIO[CohortTable, CohortUpdateFailure, Unit] =
    ZIO.accessM(_.get.update(result))
}
