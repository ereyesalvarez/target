package target.app.asset.web.manage

import org.http4k.core.Method.PUT
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization
import org.http4k.routing.bind
import org.http4k.routing.routes
import target.app.asset.domain.command.UpsertDatapointCommand
import target.app.asset.domain.model.AssetDataPoint
import target.app.asset.domain.uc.UpsertAssetDatapoint
import target.infra.http.dto.DefaultResponse
import target.infra.serializer.http4Json
import java.time.LocalDate

private val defaultResponseLens = http4Json.autoBody<DefaultResponse>().toLens()
private val requestLens = http4Json.autoBody<UpdateAssetDatapointDTO>().toLens()

fun assetDatapointRoutes(assetUpdateUC: UpsertAssetDatapoint) = routes(
  "/v1/assets/update" bind PUT to { request ->
    val input = requestLens(request)
    input.validate()

    val items = input.let {
       it.dataPoints.map { datapoint ->
         UpsertDatapointCommand(
           it.title,
           AssetDataPoint(datapoint.date ?: LocalDate.now(), datapoint.balance, datapoint.contribution, datapoint.gain)
         )
      }
    }
    assetUpdateUC.updateAssetList(items)
    Response(OK).with(defaultResponseLens of DefaultResponse("Successfully updated assets"))
  },
)
