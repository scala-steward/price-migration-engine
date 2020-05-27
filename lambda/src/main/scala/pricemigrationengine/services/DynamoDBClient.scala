package pricemigrationengine.services

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.model.{PutItemRequest, PutItemResult, QueryRequest, QueryResult, UpdateItemRequest, UpdateItemResult}
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDB, AmazonDynamoDBClient}
import zio.{ZIO, ZLayer, ZManaged}

object DynamoDBClient {
  val dynamoDB: ZLayer[Logging with DynamoDBConfiguration, String, DynamoDBClient] =
    ZLayer.fromManaged(
      ZManaged.fromFunctionM { dependencies: Logging with DynamoDBConfiguration =>
        ZManaged
          .make(
            for {
              config <- DynamoDBConfiguration.dynamoDBConfig.mapError(_.toString)
              client <- ZIO
                .effect(
                  config.endpoint
                    .foldLeft(AmazonDynamoDBClient.builder()) { (builder, endpoint) =>
                      builder.withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint.serviceEndpoint, endpoint.signingRegion)
                      )
                    }
                    .build()
                )
                .mapError(ex => s"Failed to create the dynamoDb client: $ex")
            } yield client
          ) { dynamoDB: AmazonDynamoDB =>
            ZIO
              .effect(dynamoDB.shutdown)
              .catchAll(ex => Logging.error(s"Failed to close dynamo db connection: $ex"))
          }
          .provide(dependencies)
      }
    )

  def query(queryRequest: QueryRequest): ZIO[DynamoDBClient, Throwable, QueryResult] = {
    ZIO.accessM(client => ZIO.effect(client.get.query(queryRequest)))
  }

  def updateItem(updateRequest: UpdateItemRequest): ZIO[DynamoDBClient, Throwable, UpdateItemResult] = {
    ZIO.access(_.get.updateItem(updateRequest))
  }

  def putItem(updateRequest: PutItemRequest): ZIO[DynamoDBClient, Throwable, PutItemResult] = {
    ZIO.access(_.get.putItem(updateRequest))
  }
}
