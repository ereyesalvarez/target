package target.infra.serializer

import kotlinx.serialization.json.Json
import org.http4k.format.KotlinxSerialization

val json = Json { ignoreUnknownKeys = true }

val http4Json = KotlinxSerialization