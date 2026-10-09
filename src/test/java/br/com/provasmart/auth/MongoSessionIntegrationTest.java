package br.com.provasmart.auth;

import static org.assertj.core.api.Assertions.*;

import br.com.provasmart.auth.user.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.regex.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named = "MONGODB_TEST_URI", matches = ".+")
class MongoSessionIntegrationTest {
    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> System.getenv("MONGODB_TEST_URI"));
        registry.add("app.bootstrap.enabled", () -> false);
    }

    @LocalServerPort int port;
    @Autowired UserRepository users;
    @Autowired MongoTemplate mongo;
    @Autowired PasswordEncoder encoder;
    String email;
    String id;
    HttpClient browser;

    @BeforeEach
    void setup() {
        email = UUID.randomUUID() + "@example.com";
        id =
                users.save(
                                new UserAccount(
                                        "Teste",
                                        email,
                                        encoder.encode("uma senha longa 123"),
                                        Role.ESTUDANTE))
                        .getId();
        browser =
                HttpClient.newBuilder()
                        .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                        .build();
    }

    @AfterEach
    void cleanup() {
        users.findById(id).ifPresent(users::delete);
    }

    HttpResponse<String> get(String path) throws Exception {
        return browser.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    String csrf(String html) {
        var matcher = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(html);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    HttpResponse<String> post(String path, String form) throws Exception {
        return browser.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    void login() throws Exception {
        var html = get("/login").body();
        String token = csrf(html);
        var response =
                post(
                        "/login",
                        "email="
                                + URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8)
                                + "&password=uma+senha+longa+123&_csrf="
                                + URLEncoder.encode(
                                        token, java.nio.charset.StandardCharsets.UTF_8));
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("location").orElse("")).endsWith("/painel");
    }

    @Test
    void sessionPersistsInMongoAndLogoutRemovesIt() throws Exception {
        var before = get("/login");
        var cookieHeader = before.headers().allValues("set-cookie").toString();
        assertThat(cookieHeader).contains("HttpOnly").contains("SameSite=Lax");
        login();
        assertThat(get("/estudante/painel").statusCode()).isEqualTo(200);
        var sessions = mongo.getCollection("sessions");
        var count = sessions.countDocuments();
        assertThat(count).isPositive();
        var token = csrf(get("/painel").body());
        var logout =
                post(
                        "/logout",
                        "_csrf="
                                + URLEncoder.encode(
                                        token, java.nio.charset.StandardCharsets.UTF_8));
        assertThat(logout.statusCode()).isEqualTo(302);
        assertThat(sessions.countDocuments()).isLessThan(count);
        assertThat(get("/painel").statusCode()).isEqualTo(302);
    }

    @Test
    void roleChangeRevokesSessionAndFreshLoginUsesNewRole() throws Exception {
        login();
        var account = users.findById(id).orElseThrow();
        account.setRole(Role.PROFESSOR);
        users.save(account);
        assertThat(get("/painel").headers().firstValue("location").orElse(""))
                .endsWith("/login?expired");
        login();
        assertThat(get("/professor/painel").statusCode()).isEqualTo(200);
        assertThat(get("/estudante/painel").statusCode()).isEqualTo(403);
    }

    @Test
    void disabledAccountCannotLogin() throws Exception {
        var account = users.findById(id).orElseThrow();
        account.setEnabled(false);
        users.save(account);
        var token = csrf(get("/login").body());
        assertThat(
                        post(
                                        "/login",
                                        "email="
                                                + URLEncoder.encode(
                                                        email,
                                                        java.nio.charset.StandardCharsets.UTF_8)
                                                + "&password=uma+senha+longa+123&_csrf="
                                                + token)
                                .headers()
                                .firstValue("location")
                                .orElse(""))
                .endsWith("/login?error");
    }
}
