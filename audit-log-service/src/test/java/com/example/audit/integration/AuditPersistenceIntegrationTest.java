package com.example.audit.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.audit.domain.AuditEvent;
import com.example.audit.repository.AuditEventRepository;
import com.example.audit.service.HashService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditPersistenceIntegrationTest {

    @Autowired
    private AuditEventRepository repository;

    @Autowired
    private HashService hashes;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    void persistsAndFiltersAuditRowsWithRealJpa() {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        String hash = hashes.hash(hashes.canonical(1L, "READ", "actor-1", "ACCOUNT", "account-1", "{}", timestamp));
        repository.save(new AuditEvent(1L, "READ", "actor-1", "ACCOUNT", "account-1", "{}", timestamp, "GENESIS", hash));
        repository.save(new AuditEvent(2L, "WRITE", "actor-2", "ACCOUNT", "account-2", "{}", timestamp, hash,
            hashes.hash(hashes.canonical(2L, "WRITE", "actor-2", "ACCOUNT", "account-2", "{}", timestamp))));

        Specification<AuditEvent> actorFilter = (root, query, criteria) -> criteria.equal(root.get("actorId"), "actor-1");
        var result = repository.findAll(actorFilter, PageRequest.of(0, 1));

        assertEquals(1, result.getTotalElements());
        assertEquals("actor-1", result.getContent().get(0).getActorId());
        assertEquals(2, repository.findAllByOrderByChainSequenceAsc().size());
    }

    @Test
    void databaseConstraintsRejectDuplicateChainSequence() {
        Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
        String hash = hashes.hash(hashes.canonical(1L, "READ", "actor-1", "ACCOUNT", "account-1", "{}", timestamp));
        repository.saveAndFlush(new AuditEvent(1L, "READ", "actor-1", "ACCOUNT", "account-1", "{}", timestamp, "GENESIS", hash));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> repository.saveAndFlush(
            new AuditEvent(1L, "WRITE", "actor-2", "ACCOUNT", "account-2", "{}", timestamp, hash, hash)));
    }
}
