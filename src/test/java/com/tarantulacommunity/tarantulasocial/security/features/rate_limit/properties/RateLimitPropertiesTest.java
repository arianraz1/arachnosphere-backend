package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RateLimitProperties configuration.
 * Verifies default values and property binding.
 */
class RateLimitPropertiesTest {

    @Test
    void shouldHaveDefaultLoginLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getLogin().getCapacity()).isEqualTo(10);
        assertThat(props.getLogin().getRefillPeriod()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void shouldHaveDefaultRegisterLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getRegister().getCapacity()).isEqualTo(3);
        assertThat(props.getRegister().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldHaveDefaultForgotPasswordLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getForgotPassword().getCapacity()).isEqualTo(5);
        assertThat(props.getForgotPassword().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldHaveDefaultResetPasswordLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getResetPassword().getCapacity()).isEqualTo(10);
        assertThat(props.getResetPassword().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldHaveDefaultRefreshLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getRefresh().getCapacity()).isEqualTo(15);
        assertThat(props.getRefresh().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldHaveDefaultMessageSendLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getMessageSend().getCapacity()).isEqualTo(8);
        assertThat(props.getMessageSend().getRefillPeriod()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldHaveDefaultCollectionWriteLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getCollectionWrite().getCapacity()).isEqualTo(20);
        assertThat(props.getCollectionWrite().getRefillPeriod()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldHaveDefaultProfileUpdateLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getProfileUpdate().getCapacity()).isEqualTo(8);
        assertThat(props.getProfileUpdate().getRefillPeriod()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldAllowSettingLoginLimit() {
        RateLimitProperties props = new RateLimitProperties();
        RateLimitProperties.Limit newLimit = new RateLimitProperties.Limit(5, Duration.ofMinutes(10));

        props.setLogin(newLimit);

        assertThat(props.getLogin().getCapacity()).isEqualTo(5);
        assertThat(props.getLogin().getRefillPeriod()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void shouldAllowSettingRegisterLimit() {
        RateLimitProperties props = new RateLimitProperties();
        RateLimitProperties.Limit newLimit = new RateLimitProperties.Limit(2, Duration.ofMinutes(30));

        props.setRegister(newLimit);

        assertThat(props.getRegister().getCapacity()).isEqualTo(2);
        assertThat(props.getRegister().getRefillPeriod()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void shouldCreateLimitWithConstructor() {
        RateLimitProperties.Limit limit = new RateLimitProperties.Limit(15, Duration.ofMinutes(5));

        assertThat(limit.getCapacity()).isEqualTo(15);
        assertThat(limit.getRefillPeriod()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldCreateEmptyLimitWithDefaultConstructor() {
        RateLimitProperties.Limit limit = new RateLimitProperties.Limit();

        assertThat(limit.getCapacity()).isEqualTo(0);
        assertThat(limit.getRefillPeriod()).isNull();
    }

    @Test
    void shouldSetCapacityOnLimit() {
        RateLimitProperties.Limit limit = new RateLimitProperties.Limit();
        limit.setCapacity(50);

        assertThat(limit.getCapacity()).isEqualTo(50);
    }

    @Test
    void shouldSetRefillPeriodOnLimit() {
        RateLimitProperties.Limit limit = new RateLimitProperties.Limit();
        Duration period = Duration.ofHours(2);
        limit.setRefillPeriod(period);

        assertThat(limit.getRefillPeriod()).isEqualTo(period);
    }

    @Test
    void shouldHaveAllLimitsAtInitialization() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getLogin()).isNotNull();
        assertThat(props.getRegister()).isNotNull();
        assertThat(props.getForgotPassword()).isNotNull();
        assertThat(props.getResetPassword()).isNotNull();
        assertThat(props.getRefresh()).isNotNull();
        assertThat(props.getMessageSend()).isNotNull();
        assertThat(props.getCollectionWrite()).isNotNull();
        assertThat(props.getProfileUpdate()).isNotNull();
        assertThat(props.getImageUpload()).isNotNull();
        assertThat(props.getAdminSpeciesSync()).isNotNull();
        assertThat(props.getThreadCreation()).isNotNull();
    }

    @Test
    void shouldAllowIndependentLimitModification() {
        RateLimitProperties props = new RateLimitProperties();
        int originalLoginCapacity = props.getLogin().getCapacity();

        props.setRegister(new RateLimitProperties.Limit(99, Duration.ofMinutes(99)));

        // Login limit should remain unchanged
        assertThat(props.getLogin().getCapacity()).isEqualTo(originalLoginCapacity);
        // Register limit should be updated
        assertThat(props.getRegister().getCapacity()).isEqualTo(99);
    }

    @Test
    void shouldHaveDefaultCsrfLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getCsrf().getCapacity()).isEqualTo(20);
        assertThat(props.getCsrf().getRefillPeriod()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldHaveDefaultResendVerificationLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getResendVerification().getCapacity()).isEqualTo(5);
        assertThat(props.getResendVerification().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldHaveDefaultVerifyEmailLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getVerifyEmail().getCapacity()).isEqualTo(5);
        assertThat(props.getVerifyEmail().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void shouldHaveDefaultDefaultLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getDefaultLimit().getCapacity()).isEqualTo(150);
        assertThat(props.getDefaultLimit().getRefillPeriod()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldHaveAllLimitsAtInitializationIncludingNewFields() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getLogin()).isNotNull();
        assertThat(props.getRegister()).isNotNull();
        assertThat(props.getForgotPassword()).isNotNull();
        assertThat(props.getResetPassword()).isNotNull();
        assertThat(props.getRefresh()).isNotNull();
        assertThat(props.getMessageSend()).isNotNull();
        assertThat(props.getCollectionWrite()).isNotNull();
        assertThat(props.getProfileUpdate()).isNotNull();
        assertThat(props.getCsrf()).isNotNull();
        assertThat(props.getResendVerification()).isNotNull();
        assertThat(props.getVerifyEmail()).isNotNull();
        assertThat(props.getImageUpload()).isNotNull();
        assertThat(props.getAdminSpeciesSync()).isNotNull();
        assertThat(props.getThreadCreation()).isNotNull();
        assertThat(props.getDefaultLimit()).isNotNull();
    }

    @Test
    void shouldHaveDefaultImageUploadLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getImageUpload().getCapacity()).isEqualTo(5);
        assertThat(props.getImageUpload().getRefillPeriod()).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    void shouldHaveDefaultAdminSpeciesSyncLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getAdminSpeciesSync().getCapacity()).isEqualTo(5);
        assertThat(props.getAdminSpeciesSync().getRefillPeriod()).isEqualTo(Duration.ofMinutes(5));
    }

    @Test
    void shouldHaveDefaultThreadCreationLimit() {
        RateLimitProperties props = new RateLimitProperties();

        assertThat(props.getThreadCreation().getCapacity()).isEqualTo(18);
        assertThat(props.getThreadCreation().getRefillPeriod()).isEqualTo(Duration.ofHours(1));
    }
}

