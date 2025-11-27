package yunxun.ai.canary.backend.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {

    private final Key key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(@Value("${app.security.jwt.secret:change-me}") String secret,
                      @Value("${app.security.jwt.access-token-ttl-seconds:3600}") long accessTtlSeconds,
                      @Value("${app.security.jwt.refresh-token-ttl-seconds:604800}") long refreshTtlSeconds) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTtl = Duration.ofSeconds(accessTtlSeconds);
        this.refreshTtl = Duration.ofSeconds(refreshTtlSeconds);
    }

    public String createAccessToken(Long userId, String email) {
        return buildToken(userId, email, accessTtl, "access");
    }

    public String createRefreshToken(Long userId, String email) {
        return buildToken(userId, email, refreshTtl, "refresh");
    }

    public Optional<Long> parseUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Optional.ofNullable(claims.getSubject()).map(Long::valueOf);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String buildToken(Long userId, String email, Duration ttl, String type) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("email", email, "type", type))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
