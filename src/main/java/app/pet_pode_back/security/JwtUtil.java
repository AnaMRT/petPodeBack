package app.pet_pode_back.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private static final String SECRET = "uma_chave_super_secreta_de_no_minimo_32_caracteres";
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 hora

    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static String gerarToken(UUID usuarioId) {
        return Jwts.builder()
                .setSubject(usuarioId.toString())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static UUID extrairUsuarioId(String token) {
        String subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return UUID.fromString(subject);
    }
}

