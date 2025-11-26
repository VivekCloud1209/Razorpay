package com.example.RazorpayAppliction.dto;

//package com.example.razorpaydemo.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String orderId;
    private Integer amount;
    private String currency;

    public PaymentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentResponse(boolean success, String message, String orderId, Integer amount, String currency) {
        this.success = success;
        this.message = message;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
    }
}