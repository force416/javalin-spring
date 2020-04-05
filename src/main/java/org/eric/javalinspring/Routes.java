package org.eric.javalinspring;

import com.google.gson.Gson;
import io.javalin.Javalin;
import org.eric.javalinspring.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

@Component
public class Routes {
    private final static Logger logger = LoggerFactory.getLogger(Routes.class);
    private UserController userController;
    private Gson gson = new Gson();

    public void start() {
        Javalin app = Javalin.create().start(8080);
        handleException(app);
        handleMiddleware(app);
        handlePath(app);
    }

    private void handleException(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            ctx.json(new HashMap<>() {{
                put("status", "error");
                put("message", "internal server error");
            }});
            logger.error(e.getMessage(), e);
        });
    }

    private void handleMiddleware(Javalin app) {
        app.after("/api/*", ctx -> {
            Object obj = gson.fromJson(ctx.resultString(), Object.class);
            Map<String, Object> map = new HashMap<>() {{
                put("status", "success");
                put("data", obj);
            }};
            ctx.json(map);
        });
    }

    private void handlePath(Javalin app) {
        app.routes(() -> path("/api", () -> {
            get("/user/:id", userController::getUser);
            get("/user", userController::getUsers);
        }));
    }

    @Autowired
    public void setUserController(UserController userController) {
        this.userController = userController;
    }
}
