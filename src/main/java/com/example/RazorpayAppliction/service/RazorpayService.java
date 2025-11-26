package com.example.RazorpayAppliction.service;

import com.example.RazorpayAppliction.config.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SignatureException;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class RazorpayService {

    private static final Logger logger = LoggerFactory.getLogger(RazorpayService.class);

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    @Autowired
    public RazorpayService(RazorpayConfig razorpayConfig) throws RazorpayException {
        this.razorpayConfig = razorpayConfig;
        this.razorpayClient = new RazorpayClient(
                razorpayConfig.getKeyId(),
                razorpayConfig.getKeySecret()
        );
    }

    /**
     * Create a new order in Razorpay
     */
    public Order createOrder(int amount, String currency, String receipt) throws RazorpayException {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount); // amount in paisa
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt != null ? receipt : "receipt_" + UUID.randomUUID());

            // Add payment capture setting
            orderRequest.put("payment_capture", 1);

            // Optional notes
            JSONObject notes = new JSONObject();
            notes.put("source", "spring-boot-app");
            orderRequest.put("notes", notes);

            logger.info("Creating order with amount: {} {}", (Object) amount, currency);
            Order order = razorpayClient.orders.create(orderRequest);

            String orderId = (String) order.get("id");
            logger.info("Order created successfully: {}", orderId);

            return order;

        } catch (RazorpayException e) {
            logger.error("Error creating Razorpay order: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Verify payment signature - UPDATED METHOD
     */
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            String generatedSignature = calculateRFC2104HMAC(payload, razorpayConfig.getKeySecret());

            boolean isValid = generatedSignature.equals(signature);
            logger.info("Payment verification for order {}: {}", (Object) orderId, isValid ? "VALID" : "INVALID");

            return isValid;

        } catch (Exception e) {
            logger.error("Error verifying payment signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * HMAC SHA256 signature calculation
     */
    private String calculateRFC2104HMAC(String data, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        String algorithm = "HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), algorithm);
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    /**
     * Alternative method using Razorpay's Utils (if available in your version)
     */
    public boolean verifyPaymentSignatureAlternative(String orderId, String paymentId, String signature) {
        try {
            // Try different method names that might be available
            String payload = orderId + "|" + paymentId;

            // Method 1: Try with different signature generation approach
            String generatedSignature = generateSignature(payload, razorpayConfig.getKeySecret());

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            logger.error("Error in alternative verification: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Manual signature generation
     */
    private String generateSignature(String data, String secret) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes());

            // Convert to hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error generating signature", e);
        }
    }

    /**
     * Fetch order details
     */
    public Order fetchOrder(String orderId) throws RazorpayException {
        try {
            return razorpayClient.orders.fetch(orderId);
        } catch (RazorpayException e) {
            logger.error("Error fetching order {}: {}", (Object) orderId, e.getMessage());
            throw e;
        }
    }

    /**
     * Fetch payment details
     */
    public com.razorpay.Payment fetchPayment(String paymentId) throws RazorpayException {
        try {
            return razorpayClient.payments.fetch(paymentId);
        } catch (RazorpayException e) {
            logger.error("Error fetching payment {}: {}", (Object) paymentId, e.getMessage());
            throw e;
        }
    }
}