package uz.mini.twitter.servlet;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import uz.mini.twitter.util.HibernateUtil;
import uz.mini.twitter.util.Passwords;
import uz.mini.twitter.dao.UserDao;
import uz.mini.twitter.util.PersistenceLifecycle;

import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class WebWorkflowTest {
    private String base;
    private final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();

    @Test
    void registerLoginCreateRenderEditAndDeleteThroughHttp() throws Exception {
        System.setProperty("DB_URL", "jdbc:h2:mem:web;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        System.setProperty("DB_USERNAME", "sa");
        System.setProperty("DB_PASSWORD", "test");
        System.setProperty("DB_DRIVER", "org.h2.Driver");
        System.setProperty("DB_SCHEMA_ACTION", "create-drop");
        Tomcat tomcat = new Tomcat();
        Path temp = Files.createTempDirectory("mini-twitter-web-test");
        tomcat.setBaseDir(temp.toString());
        tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        Context context = tomcat.addWebapp("/mini-twitter", Path.of("src/main/webapp").toAbsolutePath().toString());
        context.setParentClassLoader(getClass().getClassLoader());
        context.setIgnoreAnnotations(true);
        context.addApplicationListener(PersistenceLifecycle.class.getName());
        Class<?>[] servlets = {HomeServlet.class, RegisterServlet.class, LoginServlet.class,
                LogoutServlet.class, AddPostServlet.class, MyPostsServlet.class,
                EditPostServlet.class, DeletePostServlet.class};
        String[] routes = {"/home", "/register", "/login", "/logout", "/add-post", "/my-posts", "/edit-post", "/delete-post"};
        for (int i = 0; i < routes.length; i++) {
            Tomcat.addServlet(context, servlets[i].getSimpleName(), servlets[i].getName());
            context.addServletMappingDecoded(routes[i], servlets[i].getSimpleName());
        }
        try {
            tomcat.start();
            base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/mini-twitter";
            assertEquals(302, get("/add-post").statusCode());
            assertEquals(200, get("/register.jsp").statusCode());
            for (String route : new String[]{"/register", "/login", "/add-post", "/edit-post?id=1", "/delete-post?id=1", "/logout"}) {
                assertEquals(403, rawPost(route, Map.of(), null).statusCode(), route);
                assertEquals(403, rawPost(route, Map.of(), "x".repeat(43)).statusCode(), route);
            }
            assertEquals(302, post("/register", Map.of("fullName", "O‘zbek Author", "username", "author", "password", "demo-test")).statusCode());
            assertTrue(Passwords.matches("demo-test", new UserDao().findByUsername("author").getPassword()));
            String anonymousToken = token(get("/login").body());
            var login = post("/login", Map.of("username", "author", "password", "demo-test"));
            assertEquals(302, login.statusCode());
            assertTrue(login.headers().allValues("set-cookie").stream().anyMatch(value -> value.startsWith("JSESSIONID=")));
            assertNotEquals(anonymousToken, token(get("/home").body()));
            assertEquals(403, rawPost("/add-post", Map.of("title", "Stale", "description", "Stale"), anonymousToken).statusCode());
            assertEquals(200, get("/add-post").statusCode());
            assertEquals(400, post("/add-post", Map.of("title", "  ", "description", "body")).statusCode());
            assertEquals(302, post("/add-post", Map.of("title", "<script>alert(1)</script>", "description", "O‘zbekcha matn")).statusCode());
            HttpResponse<String> home = get("/home");
            assertEquals(200, home.statusCode(), home.body());
            assertTrue(home.body().contains("&lt;script&gt;"), home.body());
            assertTrue(home.body().contains("O‘zbekcha matn"));
            String mine = get("/my-posts").body();
            var idMatch = Pattern.compile("edit-post\\?id=(\\d+)").matcher(mine);
            assertTrue(idMatch.find(), mine);
            String id = idMatch.group(1);
            assertEquals(200, get("/edit-post?id=" + id).statusCode());
            assertEquals(400, get("/edit-post?id=invalid").statusCode());
            assertEquals(302, post("/edit-post?id=" + id, Map.of("title", "Updated title", "description", "Updated body")).statusCode());
            assertTrue(get("/home").body().contains("Updated title"));
            String currentToken = token(get("/login.jsp").body());
            assertEquals(302, rawPost("/edit-post?id=" + id, Map.of("title", "Updated title", "description", "Updated body"), currentToken).statusCode());
            assertEquals(403, rawPost("/edit-post?id=" + id, Map.of("title", "No", "description", "No", "_csrf", currentToken), "x".repeat(43)).statusCode());
            post("/logout", Map.of());
            post("/register", Map.of("fullName", "Another Author", "username", "other", "password", "demo-test"));
            post("/login", Map.of("username", "other", "password", "demo-test"));
            assertEquals(403, post("/delete-post?id=" + id, Map.of()).statusCode());
            assertEquals(403, post("/edit-post?id=" + id, Map.of("title", "Hijack", "description", "No")).statusCode());
            post("/logout", Map.of());
            post("/login", Map.of("username", "author", "password", "demo-test"));
            assertEquals(405, get("/delete-post?id=" + id).statusCode());
            assertEquals(405, get("/logout").statusCode());
            assertEquals(200, get("/my-posts").statusCode());
            HttpClient otherSession = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
            String foreignToken = token(otherSession.send(HttpRequest.newBuilder(URI.create(base + "/login")).GET().build(), HttpResponse.BodyHandlers.ofString()).body());
            assertEquals(403, rawPost("/delete-post?id=" + id, Map.of(), foreignToken).statusCode());
            assertTrue(get("/home").body().contains("Updated title"));
            assertEquals(302, post("/delete-post?id=" + id, Map.of()).statusCode());
            assertFalse(get("/home").body().contains("Updated title"));
        } finally {
            tomcat.stop(); tomcat.destroy(); HibernateUtil.shutdown();
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Map<String, String> values) throws Exception {
        var form = new java.util.HashMap<>(values);
        form.put("_csrf", token(get("/login.jsp").body()));
        return rawPost(path, form, null);
    }

    private String token(String html) {
        var matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
        assertTrue(matcher.find(), html);
        return matcher.group(1);
    }

    private HttpResponse<String> rawPost(String path, Map<String, String> values, String csrf) throws Exception {
        String body = values.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        var request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        if (csrf != null) request.header("X-CSRF-Token", csrf);
        return client.send(request.POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
