package br.com.provasmart.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 1800, collectionName = "sessions")
public class MongoSessionConfig {
    @Bean
    public DefaultCookieSerializer cookieSerializer(
            @Value("${COOKIE_SECURE:false}") boolean secure) {
        var cookie = new DefaultCookieSerializer();
        cookie.setCookieName("SESSION");
        cookie.setUseHttpOnlyCookie(true);
        cookie.setSameSite("Lax");
        cookie.setUseSecureCookie(secure);
        return cookie;
    }
}
