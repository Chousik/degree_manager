package ru.chousik.is.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
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

}
