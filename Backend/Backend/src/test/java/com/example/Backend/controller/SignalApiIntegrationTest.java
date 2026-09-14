package com.example.Backend.controller;

import com.example.Backend.dto.request.CreateSignalRequest;
import com.example.Backend.dto.request.LoginRequest;
import com.example.Backend.dto.request.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class SignalApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String registerAndLogin() throws Exception {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("sigtest" + unique);
        reg.setEmail("sigtest" + unique + "@example.com");
        reg.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setEmail(reg.getEmail());
        login.setPassword("StrongPass123!");
        String resp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).get("accessToken").asText();
    }

    @Test
    void createSignal_requiresAuthentication() throws Exception {
        CreateSignalRequest req = new CreateSignalRequest();
        req.setTitle("Test Signal");
        req.setDescription("A test description.");
        req.setCategory("Technology");
        req.setTags(List.of("test"));
        req.setSources(List.of());
        mockMvc.perform(post("/api/signals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndGetSignal() throws Exception {
        String token = registerAndLogin();

        CreateSignalRequest req = new CreateSignalRequest();
        req.setTitle("Integration Test Signal");
        req.setDescription("Testing signal creation end-to-end.");
        req.setCategory("Civic");
        req.setTags(List.of("integration", "test"));
        req.setSources(List.of());

        String created = mockMvc.perform(post("/api/signals")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Signal"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andReturn().getResponse().getContentAsString();

        String signalId = objectMapper.readTree(created).get("id").asText();

        mockMvc.perform(get("/api/signals/" + signalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(signalId))
                .andExpect(jsonPath("$.category").value("Civic"));
    }

    @Test
    void publicFeed_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/signals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void aiAnalysisEndpoints_returnFallbackForUnknownSignal() throws Exception {
        UUID unknownId = UUID.randomUUID();
        mockMvc.perform(get("/api/signals/" + unknownId + "/community-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.community_summary").exists());

        mockMvc.perform(get("/api/signals/" + unknownId + "/research-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conclusion").exists());

        mockMvc.perform(get("/api/signals/" + unknownId + "/comparison-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    void voteSignal_requiresAuthentication() throws Exception {
        UUID unknownId = UUID.randomUUID();
        mockMvc.perform(post("/api/signals/" + unknownId + "/vote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"voteType\":\"UP\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoints_requireAdminRole() throws Exception {
        String token = registerAndLogin();
        mockMvc.perform(get("/api/admin/signals")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportEndpoint_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"signalId\":\"" + UUID.randomUUID() + "\",\"reason\":\"SPAM\"}"))
                .andExpect(status().isUnauthorized());
    }
}
