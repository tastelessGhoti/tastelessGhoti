package com.paygate.payment.domain.payment.dto;

import com.paygate.payment.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PaymentSearchCondition {

    private String merchantId;
    private PaymentStatus status;
    private String orderId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
}
