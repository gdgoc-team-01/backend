package com.gdg.backend.infra.ai.dto;

import java.util.List;

public record AiEmbeddingResponse(List<Double> embedding) {
}
