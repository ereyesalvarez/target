package target.app.asset.uc

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import target.app.asset.domain.command.CreateAssetCommand
import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetType
import target.app.asset.domain.port.out.AssetPersistPort
import target.app.asset.domain.uc.CreateAsset
import java.util.Currency

class CreateAssetTest {

  private lateinit var assetDBPort: AssetPersistPort
  private lateinit var createAsset: CreateAsset

  @BeforeEach
  fun setup() {
    assetDBPort = mock()
    createAsset = CreateAsset(assetDBPort)
  }

  @Test
  fun `should create asset when it does not exist`() {
    val command = CreateAssetCommand(
      title = "Test Account",
      type = AssetType.CASH,
      category = AssetCategory.Cash,
      currency = Currency.getInstance("EUR")
    )

    whenever(assetDBPort.checkIfExists(command.title)).thenReturn(false)

    createAsset.execute(command)

    verify(assetDBPort).createAsset(command)
  }

  @Test
  fun `should throw exception when asset already exists`() {
    val command = CreateAssetCommand(
      title = "Duplicate Account",
      type = AssetType.CASH,
      category = AssetCategory.Cash,
      currency = Currency.getInstance("EUR")
    )

    whenever(assetDBPort.checkIfExists(command.title)).thenReturn(true)

    val ex = assertThrows<Exception> {
      createAsset.execute(command)
    }

    assertEquals("Asset with name ${command.title} already exists", ex.message)
    verify(assetDBPort, never()).createAsset(any())
  }
}