package ru.chousik.is.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.yookassa")
public class PaymentProviderProperties {

    /**
     * Shop ID issued by YooKassa.
     */
    private String shopId;

    /**
     * Secret key for basic auth.
     */
    private String secretKey;

    /**
     * Base URL for front-end return after redirect.
     */
    private String returnBaseUrl;

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getReturnBaseUrl() {
        return returnBaseUrl;
    }

    public void setReturnBaseUrl(String returnBaseUrl) {
        this.returnBaseUrl = returnBaseUrl;
    }
}
