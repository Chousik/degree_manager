package ru.chousik.is.gatewayserver.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-1)
public class RequestLoggingWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - start;
                    HttpStatusCode status = exchange.getResponse().getStatusCode();

                    log.info("{} {} -> {} ({} ms)",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            status != null ? status.value() : "N/A",
                            duration);
                });
    }
}
