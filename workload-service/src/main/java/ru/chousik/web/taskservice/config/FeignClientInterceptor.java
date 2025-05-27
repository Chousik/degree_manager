package ru.chousik.web.taskservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final OAuth2AuthorizedClientService clientService;

    public FeignClientInterceptor(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void apply(RequestTemplate template) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    oauth2Token.getAuthorizedClientRegistrationId(),
                    oauth2Token.getName()
            );

            if (client != null) {
                String token = client.getAccessToken().getTokenValue();
                template.header("Authorization", "Bearer " + token);
            }
        }
    }
}

