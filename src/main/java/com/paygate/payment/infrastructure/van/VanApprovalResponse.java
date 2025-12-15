package com.paygate.payment.infrastructure.van;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VanApprovalResponse {

    private boolean success;
    private String vanTransactionId;
    private String approvalNumber;
    private String cardCompany;
    private String responseCode;
    private String responseMessage;

    public static VanApprovalResponse success(String vanTransactionId, String approvalNumber, String cardCompany) {
        return VanApprovalResponse.builder()
                .success(true)
                .vanTransactionId(vanTransactionId)
                .approvalNumber(approvalNumber)
                .cardCompany(cardCompany)
                .responseCode("0000")
                .responseMessage("정상처리")
                .build();
    }

    public static VanApprovalResponse fail(String responseCode, String responseMessage) {
        return VanApprovalResponse.builder()
                .success(false)
                .responseCode(responseCode)
                .responseMessage(responseMessage)
                .build();
    }
}
