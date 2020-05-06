package pricemigrationengine.dynamodb

import java.util

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.{AttributeValue, QueryRequest, QueryResult}
import zio.console.Console
import zio.stream.ZStream
import zio.{IO, UIO, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object DynamoDBZIO {
  trait Service {
    def query[A](query: QueryRequest, deserializer: java.util.Map[String, AttributeValue] => IO[String, A]): UIO[ZStream[Any, String, A]]
  }

  val impl:  ZLayer[DynamoDBClient with Console, String, DynamoDBZIO] =
    ZLayer.fromFunction { dependencies: DynamoDBClient with Console =>
      new Service {
        private val console: Console.Service = dependencies.get[Console.Service]
        private val amazonDynamoDB = dependencies.get[AmazonDynamoDB]

        override def query[A](query: QueryRequest, deserializer: java.util.Map[String, AttributeValue] => IO[String, A]): UIO[ZStream[Any, String, A]] =
          for {
            _ <- console.putStrLn(s"Starting query: $query")
          } yield recursivelyExecuteQueryUntilAllResultsAreStreamed(query).mapM(deserializer)

        private def recursivelyExecuteQueryUntilAllResultsAreStreamed[A](
          query: QueryRequest
        ): ZStream[Any, String, util.Map[String, AttributeValue]] = {
          ZStream.unfoldM(Some(query).asInstanceOf[Option[QueryRequest]]) {
              case Some(queryRequest) =>
                for {
                  queryResult <- sendQueryRequest(queryRequest)
                  _ <- console.putStrLn(s"Received query results: ${queryResult.getItems}")
                  queryForNextBatch = Option(queryResult.getLastEvaluatedKey).map(lastEvaluatedKey => queryRequest.withExclusiveStartKey(lastEvaluatedKey))
                } yield Some((queryResult.getItems().asScala, queryForNextBatch))
              case None =>
                ZIO.succeed(None)
            }
            .flatMap(resultList => ZStream.fromIterable(resultList))
        }

        private def sendQueryRequest[A](queryRequest: QueryRequest): ZIO[Any, String, QueryResult] = {
          ZIO.effect {
              console.putStrLn(s"Starting query: $queryRequest")
              amazonDynamoDB.query(queryRequest)
            }
            .mapError(ex => s"Failed to execute query $queryRequest : $ex")
        }
      }
    }
}
