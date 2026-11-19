package com.gdg.backend.infra.ai.dto;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

final class AiRequestFormat {

    private AiRequestFormat() {
    }

    static OffsetDateTime kstMinute(OffsetDateTime time) {
        return time.withOffsetSameInstant(ZoneOffset.ofHours(9)).truncatedTo(ChronoUnit.MINUTES);
    }
}
