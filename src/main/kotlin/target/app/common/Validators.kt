package target.app.common

import java.math.BigDecimal

/**
 * Collection of common validation functions.
 * Throws ValidationException when validation fails.
 */
object Validators {

    // Email validation regex (RFC 5322 simplified)
    private val EMAIL_REGEX = Regex(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$"
    )

    // Username validation: alphanumeric, underscore, hyphen, 3-30 characters
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_-]{3,30}$")

    /**
     * Validates that a string is not blank.
     */
    fun requireNotBlank(value: String?, fieldName: String) {
        if (value.isNullOrBlank()) {
            throw ValidationException.fieldError(fieldName, "$fieldName is required")
        }
    }

    /**
     * Validates that a value is not null.
     */
    fun <T> requireNotNull(value: T?, fieldName: String) {
        if (value == null) {
            throw ValidationException.fieldError(fieldName, "$fieldName is required")
        }
    }

    /**
     * Validates email format.
     */
    fun validateEmail(email: String?, fieldName: String = "email") {
        requireNotBlank(email, fieldName)
        if (!EMAIL_REGEX.matches(email!!)) {
            throw ValidationException.fieldError(fieldName, "Invalid email format")
        }
    }

    /**
     * Validates username format.
     * Rules: 3-30 characters, alphanumeric with underscore and hyphen allowed.
     */
    fun validateUsername(username: String?, fieldName: String = "username") {
        requireNotBlank(username, fieldName)
        if (!USERNAME_REGEX.matches(username!!)) {
            throw ValidationException.fieldError(
                fieldName,
                "Username must be 3-30 characters and contain only letters, numbers, underscore, or hyphen"
            )
        }
    }

    /**
     * Validates password strength.
     * Rules:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     */
    fun validatePassword(password: String?, fieldName: String = "password") {
        requireNotBlank(password, fieldName)

        val minPasswordLength = 8
        val errors = mutableListOf<String>()

        if (password!!.length < minPasswordLength) {
            errors.add("at least $minPasswordLength characters")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("at least one uppercase letter")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("at least one lowercase letter")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("at least one digit")
        }

        if (errors.isNotEmpty()) {
            throw ValidationException.fieldError(
                fieldName,
                "Password must contain ${errors.joinToString(", ")}"
            )
        }
    }

    /**
     * Validates string length is within range.
     */
    @Suppress("ThrowsCount")
    fun validateLength(
        value: String?,
        fieldName: String,
        min: Int? = null,
        max: Int? = null
    ) {
        requireNotBlank(value, fieldName)

        val length = value!!.length
        when {
            min != null && max != null && (length < min || length > max) -> {
                throw ValidationException.fieldError(
                    fieldName,
                    "$fieldName must be between $min and $max characters"
                )
            }
            min != null && length < min -> {
                throw ValidationException.fieldError(
                    fieldName,
                    "$fieldName must be at least $min characters"
                )
            }
            max != null && length > max -> {
                throw ValidationException.fieldError(
                    fieldName,
                    "$fieldName must be at most $max characters"
                )
            }
        }
    }

    /**
     * Validates that a number is positive.
     */
    fun validatePositive(value: BigDecimal?, fieldName: String) {
        requireNotNull(value, fieldName)
        if (value!! <= BigDecimal.ZERO) {
            throw ValidationException.fieldError(fieldName, "$fieldName must be positive")
        }
    }

    /**
     * Validates that a number is non-negative.
     */
    fun validateNonNegative(value: BigDecimal?, fieldName: String) {
        requireNotNull(value, fieldName)
        if (value!! < BigDecimal.ZERO) {
            throw ValidationException.fieldError(fieldName, "$fieldName must be non-negative")
        }
    }

    /**
     * Validates that a value matches a regex pattern.
     */
    fun validatePattern(value: String?, fieldName: String, pattern: Regex, errorMessage: String) {
        requireNotBlank(value, fieldName)
        if (!pattern.matches(value!!)) {
            throw ValidationException.fieldError(fieldName, errorMessage)
        }
    }

    /**
     * Validates that a value is in a collection of allowed values.
     */
    fun <T> validateIn(value: T?, fieldName: String, allowedValues: Collection<T>) {
        requireNotNull(value, fieldName)
        if (value !in allowedValues) {
            throw ValidationException.fieldError(
                fieldName,
                "$fieldName must be one of: ${allowedValues.joinToString(", ")}"
            )
        }
    }
}

/**
 * Interface for objects that can be validated.
 */
interface Validatable {
    /**
     * Validates the object and throws ValidationException if invalid.
     */
    fun validate()
}
