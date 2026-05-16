package com.wealth.platform.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "浜ゆ槗濮旀墭 DTO")
public class FinTradeOrderDTO {

    @NotNull(message = "鐢ㄦ埛ID涓嶈兘涓虹┖")
    @Schema(description = "鐢ㄦ埛ID")
    private Long userId;

    @NotBlank(message = "浜у搧缂栫爜涓嶈兘涓虹┖")
    @Schema(description = "浜у搧缂栫爜")
    private String productCode;

    @NotNull(message = "浜ゆ槗绫诲瀷涓嶈兘涓虹┖")
    @Schema(description = "浜ゆ槗绫诲瀷 1涔板叆 2鍗栧嚭")
    private Integer tradeType;

    @NotNull(message = "濮旀墭浠锋牸涓嶈兘涓虹┖")
    @Schema(description = "濮旀墭浠锋牸")
    private BigDecimal entrustPrice;

    @NotNull(message = "濮旀墭鏁伴噺涓嶈兘涓虹┖")
    @Schema(description = "濮旀墭鏁伴噺")
    private Integer entrustNum;
}
