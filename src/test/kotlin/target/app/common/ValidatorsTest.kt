package target.app.common

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ValidatorsTest {

    @Test
    fun `requireNotBlank should pass for non-blank string`() {
      assertDoesNotThrow {
        Validators.requireNotBlank("test", "field")
      }
    }

    @Test
    fun `requireNotBlank should fail for null string`() {
        val exception = assertThrows<ValidationException> {
          Validators.requireNotBlank(null, "username")
        }
        assertThat(exception.message).isEqualTo("username is required")
    }

    @Test
    fun `requireNotBlank should fail for blank string`() {
        val exception = assertThrows<ValidationException> {
          Validators.requireNotBlank("   ", "username")
        }
        assertThat(exception.message).isEqualTo("username is required")
    }

    @Test
    fun `validateEmail should pass for valid email`() {
      assertDoesNotThrow {
        Validators.validateEmail("user@example.com")
        Validators.validateEmail("test.user+tag@sub.example.co.uk")
        Validators.validateEmail("user123@test-domain.com")
      }
    }

    @Test
    fun `validateEmail should fail for invalid email`() {
      assertThrows<ValidationException> {
        Validators.validateEmail("invalid-email")
      }

      assertThrows<ValidationException> {
        Validators.validateEmail("@example.com")
      }

      assertThrows<ValidationException> {
        Validators.validateEmail("user@")
      }

      assertThrows<ValidationException> {
        Validators.validateEmail("user @example.com")
      }
    }

    @Test
    fun `validateUsername should pass for valid username`() {
      assertDoesNotThrow {
        Validators.validateUsername("user123")
        Validators.validateUsername("test-user")
        Validators.validateUsername("test_user")
        Validators.validateUsername("abc")
      }
    }

    @Test
    fun `validateUsername should fail for invalid username`() {
      assertThrows<ValidationException> {
        Validators.validateUsername("ab") // Too short
      }

      assertThrows<ValidationException> {
        Validators.validateUsername("a".repeat(31)) // Too long
      }

      assertThrows<ValidationException> {
        Validators.validateUsername("user@123") // Invalid character
      }

      assertThrows<ValidationException> {
        Validators.validateUsername("user 123") // Contains space
      }
    }

    @Test
    fun `validatePassword should pass for strong password`() {
      assertDoesNotThrow {
        Validators.validatePassword("Password1")
        Validators.validatePassword("Secure123Pass")
        Validators.validatePassword("MyP@ssw0rd")
      }
    }

    @Test
    fun `validatePassword should fail for weak password`() {
      assertThrows<ValidationException> {
        Validators.validatePassword("short1A") // Too short (7 chars)
      }

      assertThrows<ValidationException> {
        Validators.validatePassword("nouppercase1") // No uppercase
      }

      assertThrows<ValidationException> {
        Validators.validatePassword("NOLOWERCASE1") // No lowercase
      }

      assertThrows<ValidationException> {
        Validators.validatePassword("NoDigitsHere") // No digits
      }
    }

    @Test
    fun `validateLength should pass for string within range`() {
      assertDoesNotThrow {
        Validators.validateLength("test", "field", min = 2, max = 10)
        Validators.validateLength("ab", "field", min = 2)
        Validators.validateLength("test", "field", max = 10)
      }
    }

    @Test
    fun `validateLength should fail for string outside range`() {
        val ex1 = assertThrows<ValidationException> {
          Validators.validateLength("a", "field", min = 2, max = 10)
        }
        assertThat(ex1.message).isEqualTo("field must be between 2 and 10 characters")

        val ex2 = assertThrows<ValidationException> {
          Validators.validateLength("a".repeat(11), "field", min = 2, max = 10)
        }
        assertThat(ex2.message).isEqualTo("field must be between 2 and 10 characters")

        val ex3 = assertThrows<ValidationException> {
          Validators.validateLength("a", "field", min = 5)
        }
        assertThat(ex3.message).isEqualTo("field must be at least 5 characters")

        val ex4 = assertThrows<ValidationException> {
          Validators.validateLength("toolong", "field", max = 5)
        }
        assertThat(ex4.message).isEqualTo("field must be at most 5 characters")
    }

    @Test
    fun `validatePositive should pass for positive number`() {
      assertDoesNotThrow {
        Validators.validatePositive(BigDecimal("1"), "amount")
        Validators.validatePositive(BigDecimal("0.01"), "amount")
        Validators.validatePositive(BigDecimal("1000"), "amount")
      }
    }

    @Test
    fun `validatePositive should fail for zero or negative number`() {
        val ex1 = assertThrows<ValidationException> {
          Validators.validatePositive(BigDecimal.ZERO, "amount")
        }
        assertThat(ex1.message).isEqualTo("amount must be positive")

        val ex2 = assertThrows<ValidationException> {
          Validators.validatePositive(BigDecimal("-1"), "amount")
        }
        assertThat(ex2.message).isEqualTo("amount must be positive")
    }

    @Test
    fun `validateNonNegative should pass for zero or positive number`() {
      assertDoesNotThrow {
        Validators.validateNonNegative(BigDecimal.ZERO, "balance")
        Validators.validateNonNegative(BigDecimal("0.01"), "balance")
        Validators.validateNonNegative(BigDecimal("1000"), "balance")
      }
    }

    @Test
    fun `validateNonNegative should fail for negative number`() {
        val exception = assertThrows<ValidationException> {
          Validators.validateNonNegative(BigDecimal("-1"), "balance")
        }
        assertThat(exception.message).isEqualTo("balance must be non-negative")
    }

    @Test
    fun `validatePattern should pass for matching pattern`() {
        val phonePattern = Regex("^\\d{3}-\\d{3}-\\d{4}$")
      assertDoesNotThrow {
        Validators.validatePattern("123-456-7890", "phone", phonePattern, "Invalid phone format")
      }
    }

    @Test
    fun `validatePattern should fail for non-matching pattern`() {
        val phonePattern = Regex("^\\d{3}-\\d{3}-\\d{4}$")
        val exception = assertThrows<ValidationException> {
          Validators.validatePattern("1234567890", "phone", phonePattern, "Invalid phone format")
        }
        assertThat(exception.message).isEqualTo("Invalid phone format")
    }

    @Test
    fun `validateIn should pass for allowed value`() {
        val allowedColors = listOf("red", "green", "blue")
      assertDoesNotThrow {
        Validators.validateIn("red", "color", allowedColors)
        Validators.validateIn("blue", "color", allowedColors)
      }
    }

    @Test
    fun `validateIn should fail for disallowed value`() {
        val allowedColors = listOf("red", "green", "blue")
        val exception = assertThrows<ValidationException> {
          Validators.validateIn("yellow", "color", allowedColors)
        }
        assertThat(exception.message).isEqualTo("color must be one of: red, green, blue")
    }

    @Test
    fun `ValidationException should have field information`() {
        val exception = ValidationException.fieldError("username", "Username is required")

        assertThat(exception.field).isEqualTo("username")
        assertThat(exception.message).isEqualTo("Username is required")
        assertThat(exception.getAllErrors()).isEqualTo(mapOf("username" to "Username is required"))
    }

    @Test
    fun `ValidationException should support multiple errors`() {
        val errors = mapOf(
            "username" to "Username is required",
            "password" to "Password is too weak"
        )
        val exception = ValidationException.multipleErrors(errors)

        assertThat(exception.hasMultipleErrors()).isEqualTo(true)
        assertThat(exception.getAllErrors()).isEqualTo(errors)
    }
}