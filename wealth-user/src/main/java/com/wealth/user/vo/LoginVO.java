package com.wealth.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "鐧诲綍杩斿洖缁撴灉")
public class LoginVO {
    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "鐢ㄦ埛ID")
    private Long userId;

    @Schema(description = "鐢ㄦ埛鏄电О")
    private String nickname;
}
