package target.infra.ratelimit

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Token bucket rate limiter that tracks requests per client identifier (e.g., IP address).
 * Thread-safe implementation using concurrent data structures.
 */
class RateLimiter(
    private val capacity: Int,
    private val refillRate: Int,
    private val refillPeriod: Duration
) {
    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    /**
     * Attempts to consume a token for the given client.
     * @param clientId Unique identifier for the client (e.g., IP address)
     * @param tokens Number of tokens to consume (default: 1)
     * @return true if tokens were consumed successfully, false if rate limit exceeded
     */
    fun tryConsume(clientId: String, tokens: Int = 1): Boolean {
        val bucket = buckets.computeIfAbsent(clientId) {
            TokenBucket(capacity, refillRate, refillPeriod)
        }
        return bucket.tryConsume(tokens)
    }

    /**
     * Clears all rate limit data for a specific client.
     */
    fun reset(clientId: String) {
        buckets.remove(clientId)
    }

    /**
     * Clears all rate limit data.
     */
    fun resetAll() {
        buckets.clear()
    }

    /**
     * Removes expired buckets to prevent memory leaks.
     * Should be called periodically (e.g., every hour).
     */
    fun cleanup(maxAge: Duration = Duration.ofHours(1)) {
        val now = Instant.now()
        buckets.entries.removeIf { (_, bucket) ->
            Duration.between(bucket.lastRefillTime, now) > maxAge
        }
    }

    private class TokenBucket(
        private val capacity: Int,
        private val refillRate: Int,
        private val refillPeriod: Duration
    ) {
        @Volatile
        private var tokens: Int = capacity

        @Volatile
        var lastRefillTime: Instant = Instant.now()
            private set

        @Synchronized
        fun tryConsume(tokensToConsume: Int): Boolean {
            refill()
            return if (tokens >= tokensToConsume) {
                tokens -= tokensToConsume
                true
            } else {
                false
            }
        }

        private fun refill() {
            val now = Instant.now()
            val timeSinceLastRefill = Duration.between(lastRefillTime, now)
            val refillPeriods = timeSinceLastRefill.toMillis() / refillPeriod.toMillis()

            if (refillPeriods > 0) {
                val tokensToAdd = (refillPeriods * refillRate).toInt()
                tokens = minOf(capacity, tokens + tokensToAdd)
                lastRefillTime = now
            }
        }
    }
}
