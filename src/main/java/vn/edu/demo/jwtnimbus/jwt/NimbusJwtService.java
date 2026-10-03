package vn.edu.demo.jwtnimbus.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import vn.edu.demo.jwtnimbus.user.AppUser;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class NimbusJwtService {
    private final byte[] secret;
    private final long expirationSeconds;
    private final String issuer;

    public NimbusJwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds,
            @Value("${security.jwt.issuer:jwt-nimbus-spring}") String issuer) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret must contain at least 32 UTF-8 bytes for HS256");
        }
        this.expirationSeconds = expirationSeconds;
        this.issuer = issuer;
    }

    public String createToken(AppUser user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(user.getUsername())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
                .jwtID(UUID.randomUUID().toString())
                .claim("roles", roles)
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(com.nimbusds.jose.JOSEObjectType.JWT).build(), claims);
        try {
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign access token", e);
        }
    }

    public String verifyAndGetSubject(String token) throws ParseException, JOSEException {
        SignedJWT jwt = SignedJWT.parse(token);
        if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(secret))) {
            throw new JOSEException("Invalid token signature or algorithm");
        }
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        Date now = new Date();
        if (!issuer.equals(claims.getIssuer()) || claims.getSubject() == null
                || claims.getExpirationTime() == null || !claims.getExpirationTime().after(now)
                || (claims.getNotBeforeTime() != null && claims.getNotBeforeTime().after(now))) {
            throw new JOSEException("Token claims are invalid or expired");
        }
        return claims.getSubject();
    }

    public long getExpirationSeconds() { return expirationSeconds; }
}
