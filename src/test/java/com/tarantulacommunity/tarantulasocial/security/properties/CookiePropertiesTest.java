package com.tarantulacommunity.tarantulasocial.security.properties;

import com.tarantulacommunity.tarantulasocial.security.config.CookieProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CookiePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldBindValidProperties() {
        runner.withPropertyValues(
                "app.cookie.domain=example.com",
                "app.cookie.secure=true",
                "app.cookie.access-token-name=access",
                "app.cookie.refresh-token-name=refresh"
        ).run(ctx -> {
            CookieProperties props = ctx.getBean(CookieProperties.class);
            assertThat(props.domain()).isEqualTo("example.com");
            assertThat(props.secure()).isTrue();
            assertThat(props.accessTokenName()).isEqualTo("access");
            assertThat(props.refreshTokenName()).isEqualTo("refresh");
        });
    }

    @Test
    void shouldFailOnBlankDomain() {
        runner.withPropertyValues(
                "app.cookie.domain=",
                "app.cookie.access-token-name=access",
                "app.cookie.refresh-token-name=refresh"
        ).run(ctx -> {
            assertThat(ctx).hasFailed();
            assertThat(ctx.getStartupFailure())
                    .rootCause()
                    .hasMessageContaining("domain");
        });
    }

    @EnableConfigurationProperties(CookieProperties.class)
    static class TestConfig {}
}