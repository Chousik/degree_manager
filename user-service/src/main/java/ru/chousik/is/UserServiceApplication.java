package ru.chousik.is;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.chousik.is.config.PaymentProviderProperties;

@SpringBootApplication
@EnableConfigurationProperties(PaymentProviderProperties.class)
public class UserServiceApplication {

	public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}
