package org.eric.javalinspring.controller;

import io.javalin.core.validation.Validator;
import io.javalin.http.Context;
import org.eric.javalinspring.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserController {

    private UserService userService;

    public void getUser(Context ctx) {
        Validator<Integer> id = ctx.pathParam("id", Integer.class);
        ctx.json(userService.getUser(id.get()));
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
