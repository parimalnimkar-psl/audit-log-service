package com.example.audit.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.audit.api.AuthController.Login;
import com.example.audit.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void tokenWithValidWriterCredentials() throws Exception {
        Login login = new Login("writer", "writer123");

        mvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.scope").value("AUDIT_WRITER AUDIT_READER"));
    }

    @Test
    void tokenWithValidReaderCredentials() throws Exception {
        Login login = new Login("reader", "reader123");

        mvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.scope").value("AUDIT_READER"));
    }

    @Test
    void tokenWithValidAdminCredentials() throws Exception {
        Login login = new Login("admin", "admin123");

        mvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.scope").value("AUDIT_WRITER AUDIT_READER AUDIT_ADMIN AUDIT_EXPORTER"));
    }

    @Test
    void tokenWithInvalidPassword() throws Exception {
        Login login = new Login("writer", "wrongpassword");

        mvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_credentials"));
    }

    @Test
    void tokenWithInvalidUsername() throws Exception {
        Login login = new Login("invaliduser", "anypassword");

        mvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_credentials"));
    }

    @Test
    void tokenHasExpirationTime() throws Exception {
        Login login = new Login("writer", "writer123");

        mvc.perform(
                        post("/auth/token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.expiresIn").exists());
    }
}
