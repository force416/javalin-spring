package org.eric.javalinspring.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Calendar;
import java.util.Date;

public class JWTUtil {

    private String secret;
    private Algorithm algorithm;
    private JWTVerifier verifier;

    private JWTUtil() {}

    public JWTUtil(String secret) {
        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(this.algorithm).build();
    }

    public String signToken() throws JWTCreationException {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, 1);

        return JWT.create().withExpiresAt(c.getTime()).sign(this.algorithm);
    }

    public DecodedJWT verify(String token) throws JWTVerificationException {
        return verifier.verify(token);
    }

}
