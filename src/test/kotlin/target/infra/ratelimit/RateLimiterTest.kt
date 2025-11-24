package target.infra.ratelimit

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class RateLimiterTest {

    private lateinit var rateLimiter: RateLimiter

    @BeforeEach
    fun setup() {
        // Create a rate limiter with 5 tokens, refilling 5 tokens per second
        rateLimiter = RateLimiter(
            capacity = 5,
            refillRate = 5,
            refillPeriod = Duration.ofSeconds(1)
        )
    }

    @Test
    fun `should allow requests within capacity`() {
        val clientId = "test-client"

        // Should allow 5 requests (full capacity)
        repeat(5) {
            assertThat(rateLimiter.tryConsume(clientId)).isTrue()
        }
    }

    @Test
    fun `should reject requests exceeding capacity`() {
        val clientId = "test-client"

        // Consume all tokens
        repeat(5) {
            rateLimiter.tryConsume(clientId)
        }

        // Next request should be rejected
        assertThat(rateLimiter.tryConsume(clientId)).isFalse()
    }

    @Test
    fun `should track different clients independently`() {
        val client1 = "client-1"
        val client2 = "client-2"

        // Client 1 consumes all tokens
        repeat(5) {
            rateLimiter.tryConsume(client1)
        }

        // Client 1 should be rate limited
        assertThat(rateLimiter.tryConsume(client1)).isFalse()

        // Client 2 should still have tokens
        assertThat(rateLimiter.tryConsume(client2)).isTrue()
    }

    @Test
    fun `should refill tokens after time period`() {
        val clientId = "test-client"

        // Consume all tokens
        repeat(5) {
            rateLimiter.tryConsume(clientId)
        }

        // Should be rate limited
        assertThat(rateLimiter.tryConsume(clientId)).isFalse()

        // Wait for refill period
        Thread.sleep(1100)

        // Should have tokens again
        assertThat(rateLimiter.tryConsume(clientId)).isTrue()
    }

    @Test
    fun `should reset client rate limit`() {
        val clientId = "test-client"

        // Consume all tokens
        repeat(5) {
            rateLimiter.tryConsume(clientId)
        }

        // Should be rate limited
        assertThat(rateLimiter.tryConsume(clientId)).isFalse()

        // Reset the client
        rateLimiter.reset(clientId)

        // Should have tokens again
        assertThat(rateLimiter.tryConsume(clientId)).isTrue()
    }

    @Test
    fun `should reset all client rate limits`() {
        val client1 = "client-1"
        val client2 = "client-2"

        // Both clients consume all tokens
        repeat(5) {
            rateLimiter.tryConsume(client1)
            rateLimiter.tryConsume(client2)
        }

        // Both should be rate limited
        assertThat(rateLimiter.tryConsume(client1)).isFalse()
        assertThat(rateLimiter.tryConsume(client2)).isFalse()

        // Reset all clients
        rateLimiter.resetAll()

        // Both should have tokens again
        assertThat(rateLimiter.tryConsume(client1)).isTrue()
        assertThat(rateLimiter.tryConsume(client2)).isTrue()
    }

    @Test
    fun `should consume multiple tokens at once`() {
        val clientId = "test-client"

        // Consume 3 tokens
        assertThat(rateLimiter.tryConsume(clientId, 3)).isTrue()

        // Should have 2 tokens left
        assertThat(rateLimiter.tryConsume(clientId, 2)).isTrue()

        // Should be out of tokens
        assertThat(rateLimiter.tryConsume(clientId, 1)).isFalse()
    }

    @Test
    fun `should reject when requesting more tokens than capacity`() {
        val clientId = "test-client"

        // Try to consume more tokens than capacity
        assertThat(rateLimiter.tryConsume(clientId, 10)).isFalse()

        // Should still have all tokens available
        assertThat(rateLimiter.tryConsume(clientId, 5)).isTrue()
    }

    @Test
    fun `should not exceed capacity after refill`() {
        val clientId = "test-client"

        // Don't consume any tokens, wait for refill period
        Thread.sleep(1100)

        // Should not have more than capacity
        assertThat(rateLimiter.tryConsume(clientId, 5)).isTrue()
        assertThat(rateLimiter.tryConsume(clientId, 1)).isFalse()
    }
}