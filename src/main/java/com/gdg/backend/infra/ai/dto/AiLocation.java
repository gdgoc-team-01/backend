package com.gdg.backend.infra.ai.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record AiLocation(BigDecimal x, BigDecimal y) {
    public AiLocation {
        x = x.setScale(2, RoundingMode.HALF_UP);
        y = y.setScale(2, RoundingMode.HALF_UP);
    }
}
