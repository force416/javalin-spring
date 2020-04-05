package org.eric.javalinspring.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Handler;
import org.eric.javalinspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserController {

    @Autowired
    private UserService userService;

    public Handler getUser() {
        return (ctx) -> {
            Validator<Integer> id = ctx.pathParam("id", Integer.class);
            ctx.json(userService.getUser(id.get()));
        };
    }

    public Handler getUsers() {
        return (ctx) -> ctx.json(userService.getUsers());
    }
}
