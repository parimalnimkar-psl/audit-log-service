package com.example.audit.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HashServiceTest {

  private HashService hashService;

  @BeforeEach
  void setUp() {
    hashService = new HashService();
  }

  @Test
  void hashIsDeterministic() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String canonical1 = hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp);
    String canonical2 = hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp);

    String hash1 = hashService.hash(canonical1);
    String hash2 = hashService.hash(canonical2);

    assertEquals(hash1, hash2);
  }

  @Test
  void hashProducesSHA256Output() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String canonical = hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp);
    String hash = hashService.hash(canonical);

    assertEquals(64, hash.length());
    assertTrue(hash.matches("[a-f0-9]{64}"));
  }

  @Test
  void changedContentProducesDifferentHash() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String hash1 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp));
    String hash2 =
        hashService.hash(
            hashService.canonical(1, "UPDATE", "user1", "ACCOUNT", "1", "{}", timestamp));

    assertNotEquals(hash1, hash2);
  }

  @Test
  void differentSequenceProducesDifferentHash() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String hash1 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp));
    String hash2 =
        hashService.hash(
            hashService.canonical(2, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp));

    assertNotEquals(hash1, hash2);
  }

  @Test
  void differentActorProducesDifferentHash() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String hash1 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp));
    String hash2 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user2", "ACCOUNT", "1", "{}", timestamp));

    assertNotEquals(hash1, hash2);
  }

  @Test
  void differentResourceProducesDifferentHash() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String hash1 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp));
    String hash2 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "2", "{}", timestamp));

    assertNotEquals(hash1, hash2);
  }

  @Test
  void differentPayloadProducesDifferentHash() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String hash1 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp));
    String hash2 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{\"key\":\"value\"}", timestamp));

    assertNotEquals(hash1, hash2);
  }

  @Test
  void differentTimestampProducesDifferentHash() {
    Instant timestamp1 = Instant.parse("2026-01-01T00:00:00Z");
    Instant timestamp2 = Instant.parse("2026-01-01T00:00:01Z");

    String hash1 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp1));
    String hash2 =
        hashService.hash(
            hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp2));

    assertNotEquals(hash1, hash2);
  }

  @Test
  void canonicalFormatIsConsistent() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String canonical = hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp);

    assertNotNull(canonical);
    assertFalse(canonical.isEmpty());
    assertTrue(canonical.contains("1"));
    assertTrue(canonical.contains("CREATE"));
  }

  @Test
  void hashIsHexadecimal() {
    Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
    String canonical = hashService.canonical(1, "CREATE", "user1", "ACCOUNT", "1", "{}", timestamp);
    String hash = hashService.hash(canonical);

    assertTrue(hash.matches("[a-f0-9]+"), "Hash should be hexadecimal");
  }
}
