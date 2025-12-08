package com.ankurshala.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.payment")
@Data
public class PaymentConfiguration {
    private RazorpayConfig razorpay = new RazorpayConfig();
    private StripeConfig stripe = new StripeConfig();
    private PayuConfig payu = new PayuConfig();
    private PaytmConfig paytm = new PaytmConfig();
    private PhonepeConfig phonepe = new PhonepeConfig();
    private GooglePayConfig googlePay = new GooglePayConfig();

    @Data
    public static class RazorpayConfig {
        private String keyId;
        private String keySecret;
        private String webhookSecret;
        private String baseUrl = "https://api.razorpay.com/v1";
        private Boolean enabled = false;
    }

    @Data
    public static class StripeConfig {
        private String publishableKey;
        private String secretKey;
        private String webhookSecret;
        private String baseUrl = "https://api.stripe.com/v1";
        private Boolean enabled = false;
    }

    @Data
    public static class PayuConfig {
        private String merchantId;
        private String merchantKey;
        private String merchantSalt;
        private String baseUrl = "https://secure.payu.in";
        private Boolean enabled = false;
    }

    @Data
    public static class PaytmConfig {
        private String merchantId;
        private String merchantKey;
        private String baseUrl = "https://securegw.paytm.in";
        private Boolean enabled = false;
    }

    @Data
    public static class PhonepeConfig {
        private String merchantId;
        private String merchantKey;
        private String baseUrl = "https://api.phonepe.com";
        private Boolean enabled = false;
    }

    @Data
    public static class GooglePayConfig {
        private String merchantId;
        private String merchantKey;
        private String baseUrl = "https://pay.google.com";
        private Boolean enabled = false;
    }
}
