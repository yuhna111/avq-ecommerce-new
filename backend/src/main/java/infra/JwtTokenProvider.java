package infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Phát và kiểm tra JWT HS256; hết hạn sau jwt.expiration-minutes (mặc định 30 phút - NFR). */
@Component
public class JwtTokenProvider {
	private final SecretKey key;
	private final long expirationMinutes;

	public JwtTokenProvider(@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-minutes:30}") long expirationMinutes) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = expirationMinutes;
	}

	public String createToken(Long userId, String role) {
		Date now = new Date();
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("role", role)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expirationMinutes * 60_000))
				.signWith(key, Jwts.SIG.HS256)
				.compact();
	}

	/** Ném io.jsonwebtoken.JwtException nếu sai chữ ký hoặc hết hạn. */
	public TokenClaims parse(String token) {
		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		return new TokenClaims(Long.valueOf(claims.getSubject()), claims.get("role", String.class));
	}

	// ===== Getter (bổ sung) =====

	public long getExpirationMinutes() {
		return expirationMinutes;
	}
}
