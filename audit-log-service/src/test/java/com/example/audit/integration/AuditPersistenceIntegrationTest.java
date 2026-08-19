package com.example.audit.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.audit.domain.AuditEvent;
import com.example.audit.api.dto.AuditEventResponse;
import com.example.audit.api.dto.CreateAuditEventRequest;
import com.example.audit.service.AuditService;
import com.example.audit.repository.AuditEventRepository;
import com.example.audit.service.HashService;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuditPersistenceIntegrationTest {

    @Autowired
    private AuditEventRepository repository;

    @Autowired
    private HashService hashes;

    @Autowired
    private AuditService auditService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
        jdbcTemplate.execute("ALTER SEQUENCE audit_chain_sequence RESTART WITH 1");
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

    @Test
    void serviceRoundTripPersistsQueriesVerifiesAndExports() {
        AuditEventResponse first = auditService.append(
            new CreateAuditEventRequest("READ", "actor-1", "ACCOUNT", "account-1", "{}"));
        AuditEventResponse second = auditService.append(
            new CreateAuditEventRequest("WRITE", "actor-1", "ACCOUNT", "account-2", "{}"));

        assertEquals(first.chainSequence() + 1, second.chainSequence());
        assertEquals(2, auditService.query("actor-1", null, null, null, null, null, PageRequest.of(0, 10)).getTotalElements());
        assertTrue(auditService.verify().intact());
        assertEquals(2, auditService.export(null, null, null, null, null, null).recordCount());
    }

    @Test
    void servicePaginationExecutesAgainstDatabase() {
        IntStream.range(0, 3).forEach(index -> auditService.append(
            new CreateAuditEventRequest("READ", "actor-" + index, "ACCOUNT", "account-" + index, "{}")));

        var page = auditService.query(null, null, null, null, null, null, PageRequest.of(0, 2));

        assertEquals(2, page.getNumberOfElements());
        assertEquals(3, page.getTotalElements());
    }

    @Test
    void concurrentServiceAppendsRemainOrderedAndVerifiable() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(6);
        try {
            List<Callable<AuditEventResponse>> tasks = IntStream.range(0, 12)
                .mapToObj(index -> (Callable<AuditEventResponse>) () -> auditService.append(
                    new CreateAuditEventRequest("READ", "actor-" + index, "ACCOUNT", "account-" + index, "{}")))
                .toList();

            List<AuditEventResponse> responses = executor.invokeAll(tasks).stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception exception) {
                        throw new AssertionError(exception);
                    }
                })
                .toList();

            assertEquals(12, responses.size());
            assertEquals(12, repository.findAllByOrderByChainSequenceAsc().size());
            assertTrue(auditService.verify().intact());
        } finally {
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void datastoreTamperingIsDetectedAfterPersistence() {
        AuditEventResponse created = auditService.append(
            new CreateAuditEventRequest("READ", "actor-1", "ACCOUNT", "account-1", "{}"));
        jdbcTemplate.update("UPDATE audit_events SET payload = ? WHERE id = ?", "{\"tampered\":true}", created.id());

        var result = auditService.verify();

        assertEquals("CONTENT_HASH_MISMATCH", result.violationType());
    }
}
