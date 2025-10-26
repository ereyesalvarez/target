package target.app.common.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ValidatorFunctionTest {

    @Test
    fun `isValidEmail should return true for valid email addresses`() {
        // Standard email formats
        assertTrue(isValidEmail("user@example.com"))
        assertTrue(isValidEmail("john.doe@company.com"))
        assertTrue(isValidEmail("user+tag@example.co.uk"))
        assertTrue(isValidEmail("user_name@example.com"))
        assertTrue(isValidEmail("user-name@example.com"))
        assertTrue(isValidEmail("user123@example.com"))
        assertTrue(isValidEmail("a@example.com"))

        // With subdomains
        assertTrue(isValidEmail("user@mail.example.com"))
        assertTrue(isValidEmail("user@mail.company.co.uk"))
    }

    @Test
    fun `isValidEmail should return false for empty string`() {
        assertFalse(isValidEmail(""))
    }

    @Test
    fun `isValidEmail should return false for blank string`() {
        assertFalse(isValidEmail(" "))
        assertFalse(isValidEmail("   "))
    }

    @Test
    fun `isValidEmail should return false for emails missing @ symbol`() {
        assertFalse(isValidEmail("userexample.com"))
        assertFalse(isValidEmail("user.example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails missing domain`() {
        assertFalse(isValidEmail("user@"))
        assertFalse(isValidEmail("user@.com"))
    }

    @Test
    fun `isValidEmail should return false for emails missing local part`() {
        assertFalse(isValidEmail("@example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails with multiple @ symbols`() {
        assertFalse(isValidEmail("user@@example.com"))
        assertFalse(isValidEmail("user@domain@example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails with spaces`() {
        assertFalse(isValidEmail("user @example.com"))
        assertFalse(isValidEmail("user@ example.com"))
        assertFalse(isValidEmail("user @example .com"))
    }

    @Test
    fun `isValidEmail should return false for emails with invalid characters`() {
        assertFalse(isValidEmail("user#example.com"))
        assertFalse(isValidEmail("user!@example.com"))
        assertFalse(isValidEmail("user*@example.com"))
        assertFalse(isValidEmail("user$@example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails starting with dot`() {
        assertFalse(isValidEmail(".user@example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails ending with dot before @`() {
        assertFalse(isValidEmail("user.@example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails with consecutive dots`() {
        assertFalse(isValidEmail("user..name@example.com"))
    }

    @Test
    fun `isValidEmail should return false for emails without TLD`() {
        assertFalse(isValidEmail("user@example"))
    }

    @Test
    fun `isValidEmail should return false for emails with invalid domain format`() {
        assertFalse(isValidEmail("user@.example.com"))
        assertFalse(isValidEmail("user@example..com"))
        assertFalse(isValidEmail("user@example."))
    }

    @Test
    fun `isValidEmail should return false for special edge cases`() {
        assertFalse(isValidEmail("@"))
        assertFalse(isValidEmail("@@"))
        assertFalse(isValidEmail(".@."))
        assertFalse(isValidEmail("a@b"))
    }
}