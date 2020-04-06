package org.eric.javalinspring;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.Javalin;
import org.eclipse.jetty.http.HttpStatus;
import org.eric.javalinspring.controller.AuthController;
import org.eric.javalinspring.controller.UserController;
import org.eric.javalinspring.exception.AuthFailException;
import org.eric.javalinspring.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;
import static org.eric.javalinspring.util.JSONUtil.*;

@Component
public class Routes {
    private final static Logger logger = LoggerFactory.getLogger(Routes.class);
    private UserController userController;
    private AuthController authController;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    public void start() {
        Javalin app = Javalin.create().start(8080);
        handleException(app);
        handleMiddleware(app);
        handlePath(app);
    }

    private void handleException(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            ctx.json(new HashMap<>() {{
                put("status", "error");
                put("message", "internal server error");
            }});
            logger.error(e.getMessage(), e);
        });

        app.exception(AuthFailException.class, (e, ctx) -> {
            Map<String, Object> map = new HashMap<>() {{
                put("status", "fail");
                put("message", "token is invalid.");
            }};
            ctx.status(HttpStatus.UNAUTHORIZED_401);
            ctx.json(map);
        });
    }

    private void handleMiddleware(Javalin app) {
        app.before("/api/*", ctx -> {
            String token = ctx.header("Authorization");
            try {
                token = token.split(" ")[1];
                JWTUtil jwt = new JWTUtil(this.jwtSecret);
                jwt.verify(token);
            } catch (JWTVerificationException | NullPointerException e) {
                throw new AuthFailException();
            }
        });

        app.after("/*", ctx -> {
            boolean isStatusOK = ctx.status() == HttpStatus.OK_200;
            boolean isJSONFormat = isJSON(ctx.resultString());

            if (isStatusOK && isJSONFormat) {
                Map<String, Object> map = new HashMap<>() {{
                    put("status", "success");
                    put("data", fromJson(ctx.resultString(), Object.class));
                }};

                ctx.json(map);
            }
        });
    }

    private void handlePath(Javalin app) {
        app.routes(() -> {

            path("/auth", () -> {
                get("/token", authController::auth);
            });

            path("/api", () -> {
                path("/user", () -> {
                    get("/:id", userController::getUser);
                    get("/", userController::getUsers);
                });
            });
        });
    }

    @Autowired
    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    @Autowired
    public void setAuthController(AuthController authController) {
        this.authController = authController;
    }
}
