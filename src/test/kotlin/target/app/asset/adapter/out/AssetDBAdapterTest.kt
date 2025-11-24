package target.app.asset.adapter.out

import assertk.assertThat
import assertk.assertions.containsExactly
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.UnableToExecuteStatementException
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import target.app.asset.domain.command.CreateAssetCommand
import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetDataPoint
import target.app.asset.domain.model.AssetType
import target.fixtures.assetA
import target.fixtures.*
import target.infra.db.getDatasource
import target.infra.properties.definition.AppDBConfig
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssetDBAdapterTest {
  private var postgres: PostgreSQLContainer<*> =
    PostgreSQLContainer("postgres:17").withInitScript("schema.sql").withReuse(true)
      .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
      .waitingFor(Wait.forListeningPort())
      .withReuse(true)

  private lateinit var jdbi: Jdbi
  private lateinit var assetDBAdapter: AssetDBAdapter
  private lateinit var dbConfig: AppDBConfig

  @BeforeAll
  fun setup() {
    postgres.start()
    dbConfig = AppDBConfig(postgres.jdbcUrl, postgres.username, postgres.password)
    val dataSource = getDatasource(dbConfig)
    jdbi = Jdbi.create(dataSource)
    assetDBAdapter = AssetDBAdapter(jdbi)
  }

  @BeforeEach
  fun clean() {
    jdbi.useHandle<Exception> { h ->
      h.execute("TRUNCATE TABLE asset_datapoint, asset RESTART IDENTITY CASCADE")
    }
  }

  @AfterAll
  fun tearDown() {
    postgres.stop()
  }

  @Test
  fun `checkIfExists returns false then true after insert`() {
    val cmd = CreateAssetCommand(
      title = "Revolut EUR",
      type = AssetType.CASH,
      category = AssetCategory.Cash,
      currency = Currency.getInstance("EUR")
    )

    val before = assetDBAdapter.checkIfExists(cmd.title)
    assertFalse(before)

    assetDBAdapter.createAsset(cmd)

    val after = assetDBAdapter.checkIfExists(cmd.title)
    assertTrue(after)
  }

  @Test
  fun `createAsset persists correct fields`() {
    val cmd = CreateAssetCommand(
      title = "Broker Account",
      type = AssetType.STOCKS,
      category = AssetCategory.Investment,
      currency = Currency.getInstance("USD")
    )

    assetDBAdapter.createAsset(cmd)

    val row = jdbi.withHandle<Map<String, Any>, Exception> { h ->
      h.createQuery(
        "SELECT title,  type_code, category_code, currency, archived, archived_at FROM asset WHERE title = :title"
      )
        .bind("title", cmd.title)
        .mapToMap()
        .one()
    }

    assertEquals("Broker Account", row["title"])
    assertEquals(AssetType.STOCKS.name, row["type_code"])
    assertEquals(AssetCategory.Investment.name, row["category_code"])
    assertEquals("USD", row["currency"])
    assertEquals(false, row["archived"])
    assertEquals(null, row["archived_at"])
  }

  @Test
  fun `duplicate name fails with constraint violation`() {
    val cmd = CreateAssetCommand(
      title = "Any",
      type = AssetType.CASH,
      category = AssetCategory.Cash,
      currency = Currency.getInstance("EUR")
    )

    assetDBAdapter.createAsset(cmd)

    assertThrows(UnableToExecuteStatementException::class.java) {
      assetDBAdapter.createAsset(cmd.copy(type = AssetType.STOCKS))
    }
  }

  @Test
  fun `upsertAssetStatus inserts new datapoint and updates existing one`() {
    val assetCmd = CreateAssetCommand(
      title = "Investment Account",
      type = AssetType.STOCKS,
      category = AssetCategory.Investment,
      currency = Currency.getInstance("USD")
    )

    assetDBAdapter.createAsset(assetCmd)
    val date = LocalDate.of(2024, 1, 15)

    val initialDatapoint = AssetDataPoint(
      date = date,
      balance = BigDecimal("1000.00"),
      gain = BigDecimal("50.00"),
      contribution = BigDecimal("950.00")
    )

    assetDBAdapter.upsertAssetStatus(assetCmd.title, initialDatapoint)

    val insertedRow = jdbi.withHandle<Map<String, Any>, Exception> { h ->
      h.createQuery(
        """
        SELECT balance, gain, contribution 
        FROM asset_datapoint ad 
        JOIN asset a ON ad.asset_id = a.id 
        WHERE a.title = :title AND ad.d = :date
      """
      )
        .bind("title", assetCmd.title)
        .bind("date", date)
        .mapToMap()
        .one()
    }

    assertEquals(BigDecimal("1000.00"), insertedRow["balance"])
    assertEquals(BigDecimal("50.00"), insertedRow["gain"])
    assertEquals(BigDecimal("950.00"), insertedRow["contribution"])

    val updatedDatapoint = AssetDataPoint(
      date = date,
      balance = BigDecimal("1100.00"),
      gain = BigDecimal("75.00"),
      contribution = BigDecimal("1025.00")
    )

    assetDBAdapter.upsertAssetStatus(assetCmd.title, updatedDatapoint)

    val updatedRow = jdbi.withHandle<Map<String, Any>, Exception> { h ->
      h.createQuery(
        """
        SELECT balance, gain, contribution 
        FROM asset_datapoint ad 
        JOIN asset a ON ad.asset_id = a.id 
        WHERE a.title = :title AND ad.d = :date
      """
      )
        .bind("title", assetCmd.title)
        .bind("date", date)
        .mapToMap()
        .one()
    }

    assertEquals(BigDecimal("1100.00"), updatedRow["balance"])
    assertEquals(BigDecimal("75.00"), updatedRow["gain"])
    assertEquals(BigDecimal("1025.00"), updatedRow["contribution"])

    val totalCount = jdbi.withHandle<Int, Exception> { h ->
      h.createQuery(
        """
        SELECT COUNT(*) 
        FROM asset_datapoint ad 
        JOIN asset a ON ad.asset_id = a.id 
        WHERE a.title = :title AND ad.d = :date
      """
      )
        .bind("title", assetCmd.title)
        .bind("date", date)
        .mapTo(Int::class.java)
        .one()
    }

    assertEquals(1, totalCount)
  }

  @Test
  fun `upsertAssetStatus throws exception for non-existing asset`() {
    val date = LocalDate.of(2024, 1, 15)

    val datapoint = AssetDataPoint(
      date = date,
      balance = BigDecimal("1000.00"),
      gain = null,
      contribution = null
    )

    val exception = assertThrows(IllegalArgumentException::class.java) {
      assetDBAdapter.upsertAssetStatus("Non-existing Asset", datapoint)
    }

    assertTrue(exception.message!!.contains("Asset with title 'Non-existing Asset' not found"))
  }

  @Test
  fun `when there are records in the database, then retrieveAssets returns them`() {
    // Create multiple assets
    val asset1 = CreateAssetCommand(
      title = "Cash Account",
      type = AssetType.CASH,
      category = AssetCategory.Cash,
      currency = Currency.getInstance("EUR")
    )

    val asset2 = CreateAssetCommand(
      title = "Investment Portfolio",
      type = AssetType.STOCKS,
      category = AssetCategory.Investment,
      currency = Currency.getInstance("USD")
    )

    val asset3 = CreateAssetCommand(
      title = "Savings Account",
      type = AssetType.SAVINGS,
      category = AssetCategory.Cash,
      currency = Currency.getInstance("EUR")
    )

    // Insert assets
    assetDBAdapter.createAsset(asset1)
    assetDBAdapter.createAsset(asset2)
    assetDBAdapter.createAsset(asset3)

    // Retrieve all assets
    val retrievedAssets = assetDBAdapter.retrieveAssets()

    // Verify results (should be ordered by title)
    assertEquals(3, retrievedAssets.size)

    // First asset (alphabetically by title)
    val cashAccount = retrievedAssets.find { it.title == "Cash Account" }!!
    assertEquals("Cash Account", cashAccount.title)
    assertEquals(AssetType.CASH, cashAccount.type)
    assertEquals(AssetCategory.Cash, cashAccount.categoryCode)
    assertEquals(Currency.getInstance("EUR"), cashAccount.currency)
    assertEquals(false, cashAccount.archived)
    assertEquals(null, cashAccount.archivedAt)

    // Second asset
    val investmentPortfolio = retrievedAssets.find { it.title == "Investment Portfolio" }!!
    assertEquals("Investment Portfolio", investmentPortfolio.title)
    assertEquals(AssetType.STOCKS, investmentPortfolio.type)
    assertEquals(AssetCategory.Investment, investmentPortfolio.categoryCode)
    assertEquals(Currency.getInstance("USD"), investmentPortfolio.currency)
    assertEquals(false, investmentPortfolio.archived)
    assertEquals(null, investmentPortfolio.archivedAt)

    // Third asset
    val savingsAccount = retrievedAssets.find { it.title == "Savings Account" }!!
    assertEquals("Savings Account", savingsAccount.title)
    assertEquals(AssetType.SAVINGS, savingsAccount.type)
    assertEquals(AssetCategory.Cash, savingsAccount.categoryCode)
    assertEquals(Currency.getInstance("EUR"), savingsAccount.currency)
    assertEquals(false, savingsAccount.archived)
    assertEquals(null, savingsAccount.archivedAt)
  }

  private fun insertDPForTimeSeries() {
    assetDBAdapter.createAsset(assetA)
    assetDBAdapter.upsertAssetStatus(assetA.title, dpA1)
    assetDBAdapter.upsertAssetStatus(assetA.title, dpA2)
    assetDBAdapter.upsertAssetStatus(assetA.title, dpA3)
    assetDBAdapter.upsertAssetStatus(assetA.title, dpA4)
    assetDBAdapter.upsertAssetStatus(assetA.title, dpA5)
    assetDBAdapter.upsertAssetStatus(assetA.title, dpA6)
    assetDBAdapter.createAsset(assetB)
    assetDBAdapter.upsertAssetStatus(assetB.title, dpB1)
    assetDBAdapter.upsertAssetStatus(assetB.title, dpB2)
    assetDBAdapter.upsertAssetStatus(assetB.title, dpB3)
    assetDBAdapter.upsertAssetStatus(assetB.title, dpB4)
    assetDBAdapter.createAsset(assetC)
  }


  @Test
  fun `retrieveMonthlyTimeSeries returns assets with monthly data points`() {
    insertDPForTimeSeries()
    // Retrieve monthly time series
    val timeSeries = assetDBAdapter.retrieveMonthlyTimeSeries()
    // Verify results
    assertEquals(3, timeSeries.size)
    val assetWithData = timeSeries.first { it.title == assetA.title }
    assertEquals(assetA.title, assetWithData.title)
    assertEquals(assetA.type, assetWithData.type)
    assertEquals(assetA.category, assetWithData.category)
    assertEquals(assetA.currency, assetWithData.currency)
    assertEquals(3, assetWithData.items.size)
    assertThat(assetWithData.items).containsExactly(dpA2, dpA4, dpA6)

    val assetWithDataB = timeSeries.first { it.title == assetB.title }
    assertEquals(assetB.title, assetWithDataB.title)
    assertEquals(assetB.type, assetWithDataB.type)
    assertEquals(assetB.category, assetWithDataB.category)
    assertEquals(assetB.currency, assetWithDataB.currency)
    assertEquals(1, assetWithDataB.items.size)
    assertThat(assetWithDataB.items).containsExactly(dpB4)

    val assetCData = timeSeries.first { it.title == assetC.title }
    assertEquals(assetC.title, assetCData.title)
    assertEquals(assetC.type, assetCData.type)
    assertEquals(assetC.category, assetCData.category)
    assertEquals(assetC.currency, assetCData.currency)
    assertEquals(0, assetCData.items.size)
  }


  @Test
  fun `retrieveDailyTimeSeries returns all assets from the date`() {
    insertDPForTimeSeries()
    val timeSeries = assetDBAdapter.retrieveDailyTimeSeries(LocalDate.parse("2024-01-01"))
    assertEquals(3, timeSeries.size)

    val assetAData = timeSeries.first { it.title == assetA.title }
    assertEquals(assetA.title, assetAData.title)
    assertEquals(assetA.type, assetAData.type)
    assertEquals(assetA.category, assetAData.category)
    assertEquals(assetA.currency, assetAData.currency)
    assertEquals(5, assetAData.items.size)
    assertThat(assetAData.items).containsExactly(dpA1, dpA2, dpA4, dpA5, dpA6)

    val assetBData = timeSeries.first { it.title == assetB.title }
    assertEquals(assetB.title, assetBData.title)
    assertEquals(assetB.type, assetBData.type)
    assertEquals(assetB.category, assetBData.category)
    assertEquals(assetB.currency, assetBData.currency)
    assertEquals(4, assetBData.items.size)
    assertThat(assetBData.items).containsExactly(dpB1, dpB2, dpB3, dpB4)
  }
  @Test
  fun `retrieveDailyTimeSeries returns all assets and filter out olders`() {
    insertDPForTimeSeries()
    val timeSeries = assetDBAdapter.retrieveDailyTimeSeries(LocalDate.parse("2025-01-01"))
    assertEquals(3, timeSeries.size)

    val assetAData = timeSeries.first { it.title == assetA.title }
    assertEquals(assetA.title, assetAData.title)
    assertEquals(assetA.type, assetAData.type)
    assertEquals(assetA.category, assetAData.category)
    assertEquals(assetA.currency, assetAData.currency)
    assertEquals(2, assetAData.items.size)
    assertThat(assetAData.items).containsExactly( dpA5, dpA6)

    val assetBData = timeSeries.first { it.title == assetB.title }
    assertEquals(assetB.title, assetBData.title)
    assertEquals(assetB.type, assetBData.type)
    assertEquals(assetB.category, assetBData.category)
    assertEquals(assetB.currency, assetBData.currency)
    assertEquals(0, assetBData.items.size)

    val assetCData = timeSeries.first { it.title == assetC.title }
    assertEquals(assetC.title, assetCData.title)
    assertEquals(assetC.type, assetCData.type)
    assertEquals(assetC.category, assetCData.category)
    assertEquals(assetC.currency, assetCData.currency)
    assertEquals(0, assetCData.items.size)
  }
  @Test
  fun `retrieveYearlyTimeSeries works`() {
    insertDPForTimeSeries()
    val timeSeries = assetDBAdapter.retrieveYearlyTimeSeries()
    assertEquals(3, timeSeries.size)

    val assetAData = timeSeries.first { it.title == assetA.title }
    assertEquals(assetA.title, assetAData.title)
    assertEquals(assetA.type, assetAData.type)
    assertEquals(assetA.category, assetAData.category)
    assertEquals(assetA.currency, assetAData.currency)
    assertEquals(2, assetAData.items.size)

    val assetBData = timeSeries.first { it.title == assetB.title }
    assertEquals(assetB.title, assetBData.title)
    assertEquals(assetB.type, assetBData.type)
    assertEquals(assetB.category, assetBData.category)
    assertEquals(assetB.currency, assetBData.currency)
    assertEquals(1, assetBData.items.size)

    val assetCData = timeSeries.first { it.title == assetC.title }
    assertEquals(assetC.title, assetCData.title)
    assertEquals(assetC.type, assetCData.type)
    assertEquals(assetC.category, assetCData.category)
    assertEquals(assetC.currency, assetCData.currency)
    assertEquals(0, assetCData.items.size)
  }
}