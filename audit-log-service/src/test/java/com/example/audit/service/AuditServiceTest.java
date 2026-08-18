package com.example.audit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.audit.api.dto.AuditEventResponse;
import com.example.audit.api.dto.CreateAuditEventRequest;
import com.example.audit.config.AppProperties;
import com.example.audit.domain.AuditEvent;
import com.example.audit.repository.AuditEventRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

  @Mock private AuditEventRepository repo;
  private AuditService service;
  private HashService hashService;
  private AppProperties props;

  @BeforeEach
  void setUp() {
    hashService = new HashService();
    props =
        new AppProperties(
            new AppProperties.Jwt("12345678901234567890123456789012", "issuer", 10),
            new AppProperties.Audit("GENESIS", 365));
    service = new AuditService(repo, hashService, props);
  }

  @Test
  void appendUsesGenesisForFirstRecord() {
    when(repo.findTopByOrderByChainSequenceDesc()).thenReturn(Optional.empty());
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

    AuditEventResponse response =
        service.append(new CreateAuditEventRequest("READ", "user1", "ACCOUNT", "1", "{}"));

    assertEquals(1L, response.chainSequence());
    assertEquals("GENESIS", response.previousHash());
    verify(repo, times(2)).findTopByOrderByChainSequenceDesc();
  }

  @Test
  void appendIncreasesSequenceForSubsequentRecords() {
    AuditEvent firstEvent =
        new AuditEvent(
            1L,
            "CREATE",
            "user1",
            "ACCOUNT",
            "1",
            "{}",
            Instant.now(),
            "GENESIS",
            "HASH1");

    when(repo.findTopByOrderByChainSequenceDesc()).thenReturn(Optional.of(firstEvent));
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

    AuditEventResponse response =
        service.append(new CreateAuditEventRequest("UPDATE", "user2", "ACCOUNT", "1", "{}"));

    assertEquals(2L, response.chainSequence());
    assertEquals("HASH1", response.previousHash());
  }

  @Test
  void appendStoresPreviousHashFromLastEvent() {
    String previousHash = "HASH123";
    AuditEvent lastEvent =
        new AuditEvent(
            5L,
            "READ",
            "user1",
            "ACCOUNT",
            "1",
            "{}",
            Instant.now(),
            "HASH122",
            previousHash);

    when(repo.findTopByOrderByChainSequenceDesc()).thenReturn(Optional.of(lastEvent));
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

    AuditEventResponse response =
        service.append(new CreateAuditEventRequest("DELETE", "admin", "ACCOUNT", "1", "{}"));

    assertEquals(previousHash, response.previousHash());
  }

  @Test
  void queryWithNoFiltersReturnsAllEvents() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    Page<AuditEventResponse> results =
        service.query(null, null, null, null, null, null, PageRequest.of(0, 10));

    assertNotNull(results);
    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void queryFiltersByActorId() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    service.query("user1", null, null, null, null, null, PageRequest.of(0, 10));

    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void queryFiltersByResourceType() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    service.query(null, "ACCOUNT", null, null, null, null, PageRequest.of(0, 10));

    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void queryFiltersByResourceId() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    service.query(null, null, "123", null, null, null, PageRequest.of(0, 10));

    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void queryFiltersByEventType() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    service.query(null, null, null, "READ", null, null, PageRequest.of(0, 10));

    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void queryFiltersByDateRange() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    Instant from = Instant.parse("2026-01-01T00:00:00Z");
    Instant to = Instant.parse("2026-12-31T23:59:59Z");

    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    service.query(null, null, null, null, from, to, PageRequest.of(0, 10));

    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void queryWithMultipleFiltersAppliesAll() {
    Page<AuditEvent> events = new PageImpl<>(List.of());
    Instant from = Instant.parse("2026-01-01T00:00:00Z");

    when(repo.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class))).thenReturn(events);

    service.query("user1", "ACCOUNT", "123", "READ", from, null, PageRequest.of(0, 10));

    verify(repo, times(1)).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
  }

  @Test
  void emptyChainVerifies() {
    when(repo.findAllByOrderByChainSequenceAsc()).thenReturn(List.of());

    var result = service.verify();

    assertTrue(result.intact());
    assertEquals(0, result.checkedRecordCount());
  }

  @Test
  void intactChainVerifies() {
    Instant now = Instant.now();
    AuditEvent event =
        new AuditEvent(
            1L,
            "CREATE",
            "user1",
            "ACCOUNT",
            "1",
            "{}",
            now,
            "GENESIS",
            hashService.hash(
                hashService.canonical(
                    1L, "CREATE", "user1", "ACCOUNT", "1", "{}", now)));

    when(repo.findAllByOrderByChainSequenceAsc()).thenReturn(List.of(event));

    var result = service.verify();

    assertTrue(result.intact());
    assertEquals(1, result.checkedRecordCount());
  }

  @Test
  void chainWithSequenceMismatchFails() {
    Instant now = Instant.now();
    AuditEvent event = new AuditEvent(2L, "CREATE", "user1", "ACCOUNT", "1", "{}", now, "GENESIS",
        "HASH");

    when(repo.findAllByOrderByChainSequenceAsc()).thenReturn(List.of(event));
    when(repo.count()).thenReturn(1L);

    var result = service.verify();

    assertFalse(result.intact());
    assertEquals("SEQUENCE_MISMATCH", result.violationType());
  }

  @Test
  void chainWithPreviousHashMismatchFails() {
    Instant now = Instant.now();
    String correctHash =
        hashService.hash(
            hashService.canonical(1L, "CREATE", "user1", "ACCOUNT", "1", "{}", now));
    AuditEvent event =
        new AuditEvent(1L, "CREATE", "user1", "ACCOUNT", "1", "{}", now, "WRONG_HASH",
            correctHash);

    when(repo.findAllByOrderByChainSequenceAsc()).thenReturn(List.of(event));
    when(repo.count()).thenReturn(1L);

    var result = service.verify();

    assertFalse(result.intact());
    assertEquals("PREVIOUS_HASH_MISMATCH", result.violationType());
  }

  @Test
  void chainWithContentHashMismatchFails() {
    Instant now = Instant.now();
    AuditEvent event =
        new AuditEvent(1L, "CREATE", "user1", "ACCOUNT", "1", "{}", now, "GENESIS", "WRONG_HASH");

    when(repo.findAllByOrderByChainSequenceAsc()).thenReturn(List.of(event));
    when(repo.count()).thenReturn(1L);

    var result = service.verify();

    assertFalse(result.intact());
    assertEquals("CONTENT_HASH_MISMATCH", result.violationType());
  }

  @Test
  void multipleEventChainVerifies() {
    Instant now = Instant.now();
    String hash1 =
        hashService.hash(
            hashService.canonical(1L, "CREATE", "user1", "ACCOUNT", "1", "{}", now));
    String hash2 =
        hashService.hash(
            hashService.canonical(2L, "UPDATE", "user1", "ACCOUNT", "1", "{}", now));

    AuditEvent event1 =
        new AuditEvent(1L, "CREATE", "user1", "ACCOUNT", "1", "{}", now, "GENESIS", hash1);
    AuditEvent event2 =
        new AuditEvent(2L, "UPDATE", "user1", "ACCOUNT", "1", "{}", now, hash1, hash2);

    when(repo.findAllByOrderByChainSequenceAsc()).thenReturn(List.of(event1, event2));

    var result = service.verify();

    assertTrue(result.intact());
    assertEquals(2, result.checkedRecordCount());
  }
}
