package com.gdg.backend.infra.ai;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "ai.server", name = "base-url")
@EnableConfigurationProperties(AiServerProperties.class)
class AiServerConfiguration {

    @Bean
    RestClient aiServerRestClient(AiServerProperties properties) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    AiServerClient aiServerClient(
            @Qualifier("aiServerRestClient") RestClient restClient
    ) {
        return new AiServerClient(restClient);
    }
}
