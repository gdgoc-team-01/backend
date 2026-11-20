package com.gdg.backend.infra.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddingOutboxRepository extends JpaRepository<EmbeddingOutbox, Long> {
}
