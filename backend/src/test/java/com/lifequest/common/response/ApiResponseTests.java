package com.lifequest.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.lifequest.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class ApiResponseTests {

    @Test
    void okBuildsStandardSuccessResponse() {
        ApiResponse<String> response = ApiResponse.ok("ready");

        assertThat(response.code()).isEqualTo(ErrorCode.OK.code());
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ready");
    }

    @Test
    void errorBuildsStandardErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.NOT_FOUND, null);

        assertThat(response.code()).isEqualTo(ErrorCode.NOT_FOUND.code());
        assertThat(response.message()).isEqualTo(ErrorCode.NOT_FOUND.defaultMessage());
        assertThat(response.data()).isNull();
    }
}
