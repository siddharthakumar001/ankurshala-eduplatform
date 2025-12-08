package com.ankurshala.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private boolean aiEnabled = false;
    private boolean paymentsEnabled = false;
    private boolean walletEnabled = false;
    
    private Payment payment = new Payment();
    
    @Data
    public static class Payment {
        private String razorpayKeyId;
        private String razorpayKeySecret;
        private String stripePublicKey;
        private String stripeSecretKey;
        private String payuMerchantKey;
        private String payuMerchantSalt;
    }

    // Additional methods for compatibility
    public Features getFeatures() {
        return new Features(aiEnabled, paymentsEnabled, walletEnabled);
    }

    @Data
    public static class Features {
        private boolean ai;
        private boolean payments;
        private boolean wallet;

        public Features(boolean ai, boolean payments, boolean wallet) {
            this.ai = ai;
            this.payments = payments;
            this.wallet = wallet;
        }
    }
}
