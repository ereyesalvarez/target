package target.infra.ratelimit

import kotlinx.serialization.json.Json
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory
import target.infra.http.dto.ErrorResponse
import java.time.Duration

private val log = LoggerFactory.getLogger("rateLimitFilter")

/**
 * Creates a rate-limiting filter that restricts the number of requests per client.
 * @param rateLimiter The rate limiter instance to use
 * @param extractClientId Function to extract client identifier from request
 *                        (defaults to X-Forwarded-For or remote address)
 */
fun rateLimitFilter(
    rateLimiter: RateLimiter,
    extractClientId: (org.http4k.core.Request) -> String = { request ->
        request.header("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
            ?: request.header("X-Real-IP")
            ?: request.source?.address
            ?: "unknown"
    }
): Filter = Filter { next ->
    { request ->
        val clientId = extractClientId(request)

        if (rateLimiter.tryConsume(clientId)) {
            next(request)
        } else {
            log.warn("Rate limit exceeded for client: $clientId on ${request.uri}")

            val errorResponse = ErrorResponse(error = "Rate limit exceeded. Please try again later.")
            Response(Status.TOO_MANY_REQUESTS)
                .header("Content-Type", "application/json")
                .header("Retry-After", "60")
                .body(Json.encodeToString(ErrorResponse.serializer(), errorResponse))
        }
    }
}

/**
 * Predefined rate limiter configurations for common use cases.
 */
object RateLimiters {
    /**
     * Strict rate limiter for authentication endpoints.
     * Allows 5 requests per minute per client.
     */
    fun authenticationLimiter(): RateLimiter {
        return RateLimiter(
            capacity = 5,
            refillRate = 5,
            refillPeriod = Duration.ofMinutes(1)
        )
    }

    /**
     * Standard rate limiter for API endpoints.
     * Allows 100 requests per minute per client.
     */
    fun apiLimiter(): RateLimiter {
        return RateLimiter(
            capacity = 100,
            refillRate = 100,
            refillPeriod = Duration.ofMinutes(1)
        )
    }

    /**
     * Permissive rate limiter for development/testing.
     * Allows 1000 requests per minute per client.
     */
    fun developmentLimiter(): RateLimiter {
        return RateLimiter(
            capacity = 1000,
            refillRate = 1000,
            refillPeriod = Duration.ofMinutes(1)
        )
    }
}
