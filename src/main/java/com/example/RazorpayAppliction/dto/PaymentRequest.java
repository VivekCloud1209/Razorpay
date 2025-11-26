package com.example.RazorpayAppliction.dto;

//package com.example.razorpaydemo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Integer amount;

    private String currency = "INR";
    private String receipt;
    private String notes;
}