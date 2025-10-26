package target.infra.http.routes

import kotlinx.serialization.json.Json
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.RequestKey
import target.infra.auth.JwtService
import target.infra.http.dto.ErrorResponse


val userIdKey = RequestKey.required<String>( "userId")

fun authFilter(jwtService: JwtService): Filter = Filter { next ->
    { request ->
        val authHeader = request.header("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Response(Status.UNAUTHORIZED).body(Json.encodeToString(ErrorResponse.serializer()))
        } else {
            val token = authHeader.removePrefix("Bearer ").trim()
            try {
                val userId = jwtService.getSubjectFromToken(token)
                val newRequest = userIdKey.inject(userId, request)
                next(newRequest)
            } catch (_: Exception) {
                Response(Status.UNAUTHORIZED).body("Invalid token")
            }
        }
    }
}
