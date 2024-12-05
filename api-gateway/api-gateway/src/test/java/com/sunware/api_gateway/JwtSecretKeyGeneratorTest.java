package com.sunware.api_gateway;

import org.junit.jupiter.api.Test;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;

public class JwtSecretKeyGeneratorTest {

    @Test
    public void generateSecretKey() {
        try {
            // Generate a secret key
            Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
            String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
            System.out.printf("\nKey = [%s]\n", encodedKey);
        } catch (Exception e) {
            System.err.println("Error generating secret key: " + e.getMessage());
        }
    }
}
