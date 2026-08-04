package com.wealth.common.utils;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConvertUtilTest {

    @Test
    void copyNonNullProperties_should_calculate_null_fields_per_instance() {
        SampleDTO firstSource = new SampleDTO();
        firstSource.setName(null);
        firstSource.setStatus(1);
        SampleEntity firstTarget = new SampleEntity();
        firstTarget.setName("old-name");
        firstTarget.setStatus(0);

        BeanConvertUtil.copyNonNullProperties(firstSource, firstTarget);

        assertThat(firstTarget.getName()).isEqualTo("old-name");
        assertThat(firstTarget.getStatus()).isEqualTo(1);

        SampleDTO secondSource = new SampleDTO();
        secondSource.setName("new-name");
        secondSource.setStatus(null);
        SampleEntity secondTarget = new SampleEntity();
        secondTarget.setName("old-name");
        secondTarget.setStatus(0);

        BeanConvertUtil.copyNonNullProperties(secondSource, secondTarget);

        assertThat(secondTarget.getName()).isEqualTo("new-name");
        assertThat(secondTarget.getStatus()).isEqualTo(0);
    }

    @Getter
    @Setter
    static class SampleDTO {
        private String name;
        private Integer status;
    }

    @Getter
    @Setter
    static class SampleEntity {
        private String name;
        private Integer status;
    }
}
