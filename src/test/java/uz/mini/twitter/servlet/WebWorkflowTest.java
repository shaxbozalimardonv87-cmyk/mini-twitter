package uz.mini.twitter.servlet;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.junit.jupiter.api.Test;
import uz.mini.twitter.filter.AuthFilter;
import uz.mini.twitter.util.HibernateUtil;
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
        FilterDef filter = new FilterDef();
        filter.setFilterName("auth"); filter.setFilter(new AuthFilter()); context.addFilterDef(filter);
        FilterMap mapping = new FilterMap(); mapping.setFilterName("auth");
        for (String route : new String[]{"/add-post", "/my-posts", "/edit-post", "/delete-post", "/logout"}) mapping.addURLPattern(route);
        context.addFilterMap(mapping);
        try {
            tomcat.start();
            base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/mini-twitter";
            assertEquals(302, get("/add-post").statusCode());
            assertEquals(302, post("/register", Map.of("fullName", "O‘zbek Author", "username", "author", "password", "demo-test")).statusCode());
            assertEquals(302, post("/login", Map.of("username", "author", "password", "demo-test")).statusCode());
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
            get("/logout");
            post("/register", Map.of("fullName", "Another Author", "username", "other", "password", "demo-test"));
            post("/login", Map.of("username", "other", "password", "demo-test"));
            assertEquals(403, get("/delete-post?id=" + id).statusCode());
            assertEquals(403, post("/edit-post?id=" + id, Map.of("title", "Hijack", "description", "No")).statusCode());
            get("/logout");
            post("/login", Map.of("username", "author", "password", "demo-test"));
            assertEquals(302, get("/delete-post?id=" + id).statusCode());
            assertFalse(get("/home").body().contains("Updated title"));
        } finally {
            tomcat.stop(); tomcat.destroy(); HibernateUtil.shutdown();
        }
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Map<String, String> values) throws Exception {
        String body = values.entrySet().stream().map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)).collect(Collectors.joining("&"));
        return client.send(HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
