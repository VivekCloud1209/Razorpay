package com.example.RazorpayAppliction.controller;

//package com.example.razorpaydemo.controller;

import com.example.RazorpayAppliction.dto.PaymentRequest;
import com.example.RazorpayAppliction.dto.PaymentResponse;
import com.example.RazorpayAppliction.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private RazorpayService razorpayService;

    /**
     * Create a new payment order
     */
    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createOrder(@Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            logger.info("Received request to create order for amount: {}", paymentRequest.getAmount());

            // Convert rupees to paisa
            int amountInPaisa = paymentRequest.getAmount() * 100;

            Order order = razorpayService.createOrder(
                    amountInPaisa,
                    paymentRequest.getCurrency(),
                    paymentRequest.getReceipt()
            );

            PaymentResponse response = new PaymentResponse(
                    true,
                    "Order created successfully",
                    order.get("id"),
                    order.get("amount"),
                    order.get("currency")
            );

            String orderId = (String) order.get("id");
            logger.info("Order created: {}", orderId);
            return ResponseEntity.ok(response);


        } catch (RazorpayException e) {
            logger.error("Failed to create order: {}", e.getMessage());
            PaymentResponse response = new PaymentResponse(false, "Failed to create order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error creating order: {}", e.getMessage());
            PaymentResponse response = new PaymentResponse(false, "Internal server error");
            return ResponseEntity.internalServerError().body(response);
        }
    }


    /**
     * Verify payment after completion
     */
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @RequestParam String razorpay_order_id,
            @RequestParam String razorpay_payment_id,
            @RequestParam String razorpay_signature) {

        try {
            logger.info("Verifying payment for order: {}", razorpay_order_id);

            boolean isValid = razorpayService.verifyPaymentSignature(
                    razorpay_order_id,
                    razorpay_payment_id,
                    razorpay_signature
            );

            if (isValid) {
                PaymentResponse response = new PaymentResponse(true, "Payment verified successfully");
                logger.info("Payment verified successfully for order: {}", razorpay_order_id);
                return ResponseEntity.ok(response);
            } else {
                PaymentResponse response = new PaymentResponse(false, "Payment verification failed");
                logger.warn("Payment verification failed for order: {}", razorpay_order_id);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            logger.error("Error verifying payment: {}", e.getMessage());
            PaymentResponse response = new PaymentResponse(false, "Error verifying payment");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get order details
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable String orderId) {
        try {
            Order order = razorpayService.fetchOrder(orderId);
            return ResponseEntity.ok(order.toString());
        } catch (RazorpayException e) {
            logger.error("Error fetching order {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(new PaymentResponse(false, "Error fetching order details"));
        }
    }
}