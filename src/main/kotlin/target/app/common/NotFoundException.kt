package target.app.common

class NotFoundException(override val message: String?) : RuntimeException(message)
