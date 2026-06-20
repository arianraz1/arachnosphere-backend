package com.tarantulacommunity.tarantulasocial.features.admin.gbif;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.data.GbifTaxon;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GbifClientTest {

    @Autowired
    private GbifClient gbifClient;

    @Test
    void getFamilyKey_shouldReturnOptional_whenCalled() {
        Optional<Long> result = gbifClient.getFamilyKey("Theraphosidae");
        assertThat(result).contains(5650L);
    }

    @Test
    void getFamilyKey_shouldReturnEmpty_forInvalidFamily() {
        Optional<Long> result = gbifClient.getFamilyKey("InvalidFamilyNameThatDoesNotExist");

        // Should return empty for invalid family names
        assertThat(result).isEmpty();
    }

    @Test
    void fetchAllSpeciesByFamily_shouldReturnList_whenCalled() {
        Optional<Long> familyKey = gbifClient.getFamilyKey("Theraphosidae");
        assertThat(familyKey).isPresent();

        List<GbifTaxon> result = gbifClient.fetchAllSpeciesByFamily(familyKey.get());
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(List.class);
    }

    @Test
    void fetchAllSpeciesByFamily_shouldReturnEmptyList_forInvalidFamilyKey() {
        // Test with an invalid family key
        List<GbifTaxon> result = gbifClient.fetchAllSpeciesByFamily(-1L);

        // Should return empty list for invalid family key
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
