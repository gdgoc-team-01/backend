package com.gdg.backend.infra.ai;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("ai.server")
public record AiServerProperties(
        URI baseUrl,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("30s") Duration readTimeout
) {
}
