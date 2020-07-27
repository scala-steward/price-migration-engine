package pricemigrationengine.model

import java.time.LocalDate

import com.amazonaws.services.dynamodbv2.model.AttributeValue

import scala.jdk.CollectionConverters._

class CohortSpecTest extends munit.FunSuite {

  private val importStartDate = LocalDate.of(2020, 5, 1)
  private val migrationCompleteDate = LocalDate.of(2020, 6, 18)

  private def isActive(onDay: LocalDate, migrationComplete: Option[LocalDate] = Some(migrationCompleteDate)) =
    CohortSpec.isActive(
      CohortSpec(
        cohortName = "name",
        brazeCampaignName = "cmp123",
        importStartDate,
        earliestPriceMigrationStartDate = LocalDate.of(2021, 1, 1),
        migrationComplete
      )
    )(onDay)

  test("isActive: should be false when given date is before import start date") {
    assertEquals(isActive(importStartDate.minusDays(3)), false)
  }

  test("isActive: should be true when given date is import start date") {
    assertEquals(isActive(importStartDate), true)
  }

  test("isActive: should be true when given date is between import start date and migration complete date") {
    assertEquals(isActive(importStartDate.plusDays(3)), true)
  }

  test("isActive: should be false when given date is on migration complete date") {
    assertEquals(isActive(migrationCompleteDate), false)
  }

  test("isActive: should be false when given date is after migration complete date") {
    assertEquals(isActive(migrationCompleteDate.plusDays(3)), false)
  }

  test("isActive: should be true when given date is after import start date and migration isn't complete") {
    assertEquals(isActive(importStartDate.plusDays(3), migrationComplete = None), true)
  }

  test("tableName: should be tmpTableName field value when present") {
    val cohortSpec = CohortSpec(
      cohortName = "name",
      brazeCampaignName = "cmp123",
      importStartDate = LocalDate.of(2020, 1, 1),
      earliestPriceMigrationStartDate = LocalDate.of(2020, 1, 1),
      migrationCompleteDate = None,
      tmpTableName = Some("givenName")
    )
    assertEquals(cohortSpec.tableName(stage = "DEV"), "givenName")
  }

  test("tableName: should be transformed cohort name when tmpTableName field value not present") {
    val cohortSpec = CohortSpec(
      cohortName = "Home Delivery 2018",
      brazeCampaignName = "cmp123",
      importStartDate = LocalDate.of(2020, 1, 1),
      earliestPriceMigrationStartDate = LocalDate.of(2020, 1, 1),
      migrationCompleteDate = None,
      tmpTableName = None
    )
    assertEquals(cohortSpec.tableName(stage = "PROD"), "PriceMigration-PROD-HomeDelivery2018")
  }

  test("fromDynamoDbItem: should include all fields") {
    val item = Map(
      "cohortName" -> new AttributeValue().withS("Home Delivery 2018"),
      "brazeCampaignName" -> new AttributeValue().withS("cmp123"),
      "importStartDate" -> new AttributeValue().withS("2020-01-01"),
      "earliestPriceMigrationStartDate" -> new AttributeValue().withS("2020-01-01"),
      "tmpTableName" -> new AttributeValue().withS("tmpName")
    ).asJava
    val cohortSpec = CohortSpec.fromDynamoDbItem(item)
    assertEquals(
      cohortSpec,
      Right(
        CohortSpec(
          cohortName = "Home Delivery 2018",
          brazeCampaignName = "cmp123",
          importStartDate = LocalDate.of(2020, 1, 1),
          earliestPriceMigrationStartDate = LocalDate.of(2020, 1, 1),
          migrationCompleteDate = None,
          tmpTableName = Some("tmpName")
        )
      )
    )
  }
}
