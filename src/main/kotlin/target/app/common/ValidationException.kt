package target.app.common

/**
 * Exception thrown when input validation fails.
 * @param message The validation error message
 * @param field The field that failed validation (optional)
 * @param errors Multiple validation errors (optional)
 */
class ValidationException(
    message: String,
    val field: String? = null,
    val errors: Map<String, String> = emptyMap()
) : RuntimeException(message) {

    companion object {
        /**
         * Creates a ValidationException for a single field error.
         */
        fun fieldError(field: String, message: String): ValidationException {
            return ValidationException(
                message = message,
                field = field,
                errors = mapOf(field to message)
            )
        }

        /**
         * Creates a ValidationException for multiple field errors.
         */
        fun multipleErrors(errors: Map<String, String>): ValidationException {
            val message = errors.entries.joinToString("; ") { "${it.key}: ${it.value}" }
            return ValidationException(
                message = "Validation failed: $message",
                errors = errors
            )
        }
    }

    /**
     * Returns true if this exception has multiple errors.
     */
    fun hasMultipleErrors(): Boolean = errors.size > 1

    /**
     * Returns all validation errors as a map.
     */
    fun getAllErrors(): Map<String, String> = errors
}
