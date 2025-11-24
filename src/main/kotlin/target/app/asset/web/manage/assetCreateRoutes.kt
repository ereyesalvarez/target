package target.app.asset.web.manage

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import org.http4k.routing.routes
import target.app.asset.domain.command.CreateAssetCommand
import target.app.asset.domain.uc.CreateAsset
import target.infra.http.dto.DefaultResponse
import target.infra.serializer.http4Json
import java.util.*

private val defaultResponseLens = http4Json.autoBody<DefaultResponse>().toLens()
private val requestLens = http4Json.autoBody<CreateAssetDTO>().toLens()

fun assetCreateRoutes(createAsset: CreateAsset) = routes(
  "/v1/assets" bind Method.POST to { request ->
    val dto = requestLens(request)
    dto.validate()
    val command = dto.toDomain()
    try {
      createAsset.execute(command)
      Response(OK).with(defaultResponseLens of DefaultResponse("Asset created successfully"))
    } catch (e: Exception) {
      val response = DefaultResponse(e.message ?: "Something went wrong")
      Response(BAD_REQUEST).with(defaultResponseLens of response)
    }
  })


private fun CreateAssetDTO.toDomain() = CreateAssetCommand(
  title, type, category, Currency.getInstance(currency)
)
