package org.eric.javalinspring;

import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.websocket.WsContext;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.javalin.apibuilder.ApiBuilder.*;
import static org.eric.javalinspring.util.JSONUtil.*;

@Component
public class Routes {
    private final static Logger logger = LoggerFactory.getLogger(Routes.class);
    private UserController userController;
    private AuthController authController;

    private Map<WsContext, String> wsMap = new ConcurrentHashMap<>();
    private AtomicInteger nextUserNumber = new AtomicInteger();

    @Value("${JWT_SECRET}")
    private String JWT_SECRET;

    @Value("${SERVER_PORT}")
    private int SERVER_PORT;

    @Value("${PUBLIC_PAGE_PATH}")
    private String PUBLIC_PAGE_PATH;

    public void start() {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles(PUBLIC_PAGE_PATH);
        }).start(SERVER_PORT);

        handleException(app);
        handleMiddleware(app);
        handlePath(app);
    }

    private void handleException(Javalin app) {
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR_500);
            ctx.json(new HashMap<>() {{
                put("status", "error");
                put("message", "internal server error.");
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
        Handler jwtAuthHandler = ctx -> {
            String token = ctx.header("Authorization");
            try {
                token = token.split(" ")[1];
                JWTUtil jwt = new JWTUtil(JWT_SECRET);
                jwt.verify(token);
            } catch (JWTVerificationException | NullPointerException e) {
                throw new AuthFailException();
            }
        };
        app.before("/api/user/*", jwtAuthHandler);

        Handler jSendHandler = ctx -> {
            boolean isStatusOK = ctx.status() == HttpStatus.OK_200;
            boolean isJSONFormat = isJSON(ctx.resultString());

            if (isStatusOK && isJSONFormat) {
                Map<String, Object> map = new HashMap<>() {{
                    put("status", "success");
                    put("data", fromJson(ctx.resultString(), Object.class));
                }};

                ctx.json(map);
            }
        };
        app.after("/api/*", jSendHandler);
    }

    private void handlePath(Javalin app) {
        app.routes(() -> {
            path("/api", () -> {
                path("/auth", () -> {
                    get("/token", authController::auth);
                });
                path("/user", () -> {
                    get("/:id", userController::getUser);
                });
            });
        });

        app.ws("/chat", ws -> {
            ws.onConnect(ctx -> {
                String username = "User" + nextUserNumber.incrementAndGet();
                broadcastMessage("Server", (username + " join the chat"));
                wsMap.put(ctx, username);
            });
            ws.onClose(ctx -> {
                String username = wsMap.get(ctx);
                wsMap.remove(ctx);
                broadcastMessage("Server", (username + " left the chat"));
            });
            ws.onMessage(ctx -> {
                broadcastMessage(wsMap.get(ctx), ctx.message());
            });
        });
    }

    private void broadcastMessage(String sender, String message) {
        wsMap.keySet().stream().filter(ctx -> ctx.session.isOpen()).forEach(session -> {
            session.send(
                    new HashMap<>(){{
                        put("message", message);
                        put("sender", sender);
                    }}
            );
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
