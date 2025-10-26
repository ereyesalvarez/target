package target.infra.auth
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import java.time.Duration
import java.util.*
import javax.crypto.SecretKey


class JwtService(private val issuer: String , private val key: SecretKey? = Jwts.SIG.HS256.key().build()) {
    // ToDo: implement secret key
    val expirationTime: Long = Duration.ofHours(6).toMillis()

    fun createToken(userId: String, roles: List<String> = listOf("ADMIN")): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationTime)
        return Jwts.builder()
            .subject(userId)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiryDate)
            .issuer(issuer)
            .signWith(key)
            .compact()
    }

    /**
     * Verify and parse the token to get the subject
     * throws SignatureException if the verification fails
     */
    fun getSubjectFromToken(token: String?): String {
        return parseClaims(token).subject
    }

    fun getRolesFromToken(token: String?): List<String> {
        val claims = parseClaims(token)
        val rolesClaim = claims["roles"]
        return when (rolesClaim) {
            is Collection<*> -> rolesClaim.mapNotNull { it?.toString() }
            is Array<*> -> rolesClaim.mapNotNull { it?.toString() }
            is String -> rolesClaim.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }

    private fun parseClaims(token: String?): Claims =
        Jwts.parser()
            .requireIssuer(issuer)
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
