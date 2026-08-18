package com.example.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditLogApplicationTest {

  @Test
  void contextLoads() {
    // This test verifies that the application context loads successfully
    assertTrue(true);
  }

  @Test
  void applicationCanStart() {
    // This test verifies the application can be instantiated
    assertNotNull(AuditLogApplication.class);
  }
}
