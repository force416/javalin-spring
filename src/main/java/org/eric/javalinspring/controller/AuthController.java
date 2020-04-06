package org.eric.javalinspring.controller;

import io.javalin.http.Context;
import org.eric.javalinspring.util.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthController {

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    public void auth(Context ctx) {
        JWTUtil jwt = new JWTUtil(jwtSecret);
        ctx.json(String.format("Bearer %s", jwt.signToken()));
    }
}
