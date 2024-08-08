package ru.sb.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {

    private static final SecretKey KEY = Jwts.SIG.HS256.key().build();
    private static final long TOKEN_LIFETIME_IN_SECONDS = 24 * 60 * 60;

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TOKEN_LIFETIME_IN_SECONDS * 1000))
                .issuer("sb.ru")
                .signWith(KEY)
                .compact();
    }

    public String getSubject(String token) {
        if (isTokenExpired(token)) {
            return null;
        }
        return getClaims(token, Claims::getSubject);
    }

    public <T> T getClaims(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload());
    }

    public boolean isTokenExpired(String token) {
        return getClaims(token, Claims::getExpiration).before(new Date());
    }

}