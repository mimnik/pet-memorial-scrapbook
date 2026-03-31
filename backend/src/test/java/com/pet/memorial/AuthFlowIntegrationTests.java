package com.pet.memorial;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterLoginAndAccessProtectedApi() throws Exception {
        String registerRequest = """
            {
              "username": "tester",
              "email": "tester@example.com",
              "password": "123456"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
            .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(registerRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").isNotEmpty());

        String loginRequest = """
            {
              "username": "tester",
              "password": "123456"
            }
            """;

        String loginResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(loginRequest))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode root = objectMapper.readTree(loginResponse);
        String token = root.path("data").path("token").asText();

        mockMvc.perform(get("/api/pets")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldRejectProtectedApiWithoutToken() throws Exception {
        mockMvc.perform(get("/api/pets"))
            .andExpect(status().isUnauthorized());
    }
}
