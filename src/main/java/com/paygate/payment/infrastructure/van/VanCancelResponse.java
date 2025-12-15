package com.paygate.payment.infrastructure.van;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VanCancelResponse {

    private boolean success;
    private String vanCancelId;
    private String responseCode;
    private String responseMessage;

    public static VanCancelResponse success(String vanCancelId) {
        return VanCancelResponse.builder()
                .success(true)
                .vanCancelId(vanCancelId)
                .responseCode("0000")
                .responseMessage("정상처리")
                .build();
    }

    public static VanCancelResponse fail(String responseCode, String responseMessage) {
        return VanCancelResponse.builder()
                .success(false)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }
}
