package com.tarantulacommunity.tarantulasocial.security.properties;

import com.tarantulacommunity.tarantulasocial.security.config.JwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldBindValidProperties() {
        runner.withPropertyValues(
                "app.jwt.secret=dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGZvciBqd3QgdGVzdGluZw==", // 32+ bytes
                "app.jwt.access-token-expiration=PT15M",
                "app.jwt.refresh-token-expiration=P7D"
        ).run(ctx -> {
            JwtProperties props = ctx.getBean(JwtProperties.class);
            assertThat(props.secret()).isNotBlank();
            assertThat(props.accessTokenExpiration()).isEqualTo(java.time.Duration.ofMinutes(15));
            assertThat(props.refreshTokenExpiration()).isEqualTo(java.time.Duration.ofDays(7));
        });
    }

    @Test
    void shouldFailOnBlankSecret() {
        runner.withPropertyValues("app.jwt.secret=").run(ctx -> {
            assertThat(ctx).hasFailed();
        });
    }

    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {}
}
