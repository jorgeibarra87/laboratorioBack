package laboratorio.security;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${security.jwt.key.private}")
    private String secretoJwt;

    public DecodedJWT validateToken (String token) {
        Algorithm algorithm = Algorithm.HMAC256(secretoJwt);
        JWTVerifier verifier = com.auth0.jwt.JWT.require(algorithm).build();
        return verifier.verify(token);
    }

    public List<GrantedAuthority> getAuthorities(DecodedJWT jwt) {
        String rolesString = jwt.getClaim("authorities").asString();
        if (rolesString == null || rolesString.isEmpty()) return List.of();

        return Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}