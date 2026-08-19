package com.example.audit.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.audit.api.dto.UserResponse;
import com.example.audit.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(authorities = "SCOPE_AUDIT_ADMIN")
    void adminScopeCanListUsers() throws Exception {
        when(userService.listActiveUsers()).thenReturn(List.of());

        mvc.perform(get("/api/users"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_AUDIT_READER")
    void readerScopeCannotListUsers() throws Exception {
        mvc.perform(get("/api/users"))
            .andExpect(status().isForbidden());
    }
}
