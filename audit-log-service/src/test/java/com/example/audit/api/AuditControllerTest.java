package com.example.audit.api;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.audit.api.dto.AuditEventResponse;
import com.example.audit.api.dto.CreateAuditEventRequest;
import com.example.audit.api.dto.VerificationResponse;
import com.example.audit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

  @Autowired private MockMvc mvc;

  @MockBean private AuditService service;

  @Autowired private ObjectMapper mapper;

  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_WRITER")
  void createEventWithValidAuthority() throws Exception {
    CreateAuditEventRequest req =
        new CreateAuditEventRequest("CREATE", "user1", "ACCOUNT", "123", "{}");
    AuditEventResponse resp =
        new AuditEventResponse(
            1L,
            1L,
            "CREATE",
            "user1",
            "ACCOUNT",
            "123",
            "{}",
            Instant.parse("2026-01-01T00:00:00Z"),
            "GENESIS",
            "HASH1",
            null);

    when(service.append(req)).thenReturn(resp);

    mvc.perform(
            post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.chainSequence").value(1));

    verify(service, times(1)).append(req);
  }

  @Test
  void createEventWithoutAuthShouldFail() throws Exception {
    CreateAuditEventRequest req =
        new CreateAuditEventRequest("CREATE", "user1", "ACCOUNT", "123", "{}");

    mvc.perform(
            post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_READER")
  void queryEventsWithValidAuthority() throws Exception {
    Page<AuditEventResponse> page = new PageImpl<>(java.util.List.of());

    when(service.query(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(Instant.class), nullable(Instant.class), any()))
        .thenReturn(page);

    mvc.perform(get("/audit/events").param("actorId", "user1"))
        .andExpect(status().isOk());

    verify(service, times(1)).query(nullable(String.class), nullable(String.class), nullable(String.class), nullable(String.class), nullable(Instant.class), nullable(Instant.class), any());
  }

  @Test
  void queryEventsWithoutAuthShouldFailUnauthorized() throws Exception {
    mvc.perform(get("/audit/events")).andExpect(status().isUnauthorized());
  }



  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_ADMIN")
  void verifyEventsWithValidAuthority() throws Exception {
    VerificationResponse resp = new VerificationResponse(true, 1, null, null, null, "All records verified");

    when(service.verify()).thenReturn(resp);

    mvc.perform(get("/audit/verify"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.intact").value(true));

    verify(service, times(1)).verify();
  }

  @Test
  void verifyEventsWithoutAuthShouldFail() throws Exception {
    mvc.perform(get("/audit/verify")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_WRITER")
  void createEventWithInvalidRequestShouldFailValidation() throws Exception {
    String invalidJson = "{\"eventType\":\"\"}";

    mvc.perform(
            post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }
}
