package com.wealth.platform.message.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "绔欏唴娑堟伅 DTO")
public class FinMessageDTO {

    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID")
    private Long userId;

    @Schema(description = "娑堟伅绫诲瀷 1绯荤粺 2浜ゆ槗 3椋庢帶")
    private Integer msgType;

    @NotBlank(message = "娑堟伅鏍囬涓嶈兘涓虹┖")
    @Schema(description = "娑堟伅鏍囬")
    private String msgTitle;

    @NotBlank(message = "娑堟伅鍐呭涓嶈兘涓虹┖")
    @Schema(description = "娑堟伅鍐呭")
    private String msgContent;
}
