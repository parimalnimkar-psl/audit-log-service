package com.example.audit.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

  @Autowired private MockMvc mvc;

  @Autowired private ObjectMapper mapper;

  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_WRITER")
  void validationErrorReturnsBadRequestWithFieldErrors() throws Exception {
    // Create invalid request with missing required fields - null eventType
    String invalidJson = "{\"eventType\":null,\"actorId\":\"user\",\"resourceType\":\"ACCOUNT\",\"resourceId\":\"1\",\"payload\":\"{}\"}";

    mvc.perform(
            post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("validation_failed"))
        .andExpect(jsonPath("$.fields").exists());
  }

  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_WRITER")
  void validationErrorHandlesMultipleFieldErrors() throws Exception {
    // Create invalid request with multiple missing fields
    String invalidJson = "{\"eventType\":null,\"actorId\":null}";

    mvc.perform(
            post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("validation_failed"));
  }

  @Test
  @WithMockUser(authorities = "SCOPE_AUDIT_WRITER")
  void validationErrorIncludesFieldMessages() throws Exception {
    String invalidJson = "{\"eventType\":\"\"}";

    mvc.perform(
            post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("validation_failed"))
        .andExpect(jsonPath("$.fields").isMap());
  }
}
