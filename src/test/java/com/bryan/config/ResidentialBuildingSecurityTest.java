package com.bryan.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.flyway.enabled=false")
@AutoConfigureMockMvc
class ResidentialBuildingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsPublicAccessToActiveBuildings() throws Exception {
        mockMvc.perform(get("/api/v1/residential-buildings/active"))
            .andExpect(status().isOk());
    }
}
