package com.anoop.videoStream.config;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@Component
public class TelegramWebClientConfig {

    private WebClient webClient;

    @PostConstruct
    public void init() {

        System.out.println("Telegram setup done");
         ExchangeStrategies strategies =
            ExchangeStrategies.builder()
                    .codecs(configurer ->
                            configurer.defaultCodecs()
                                    .maxInMemorySize(
                                            50 * 1024 * 1024
                                    ))
                    .build();

        this.webClient =
                WebClient.builder()
                        .baseUrl("https://api.telegram.org")
                        .exchangeStrategies(strategies)
                        .build();
    }

    public WebClient getWebClient() {
        return webClient;
    }
}