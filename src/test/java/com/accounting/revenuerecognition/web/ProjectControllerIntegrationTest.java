package com.accounting.revenuerecognition.web;

import com.accounting.revenuerecognition.domain.enums.PricingModel;
import com.accounting.revenuerecognition.domain.enums.ProjectStatus;
import com.accounting.revenuerecognition.dto.request.CreateProjectRequest;
import com.accounting.revenuerecognition.dto.response.ProjectResponse;
import com.accounting.revenuerecognition.service.ContractProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ContractProjectService projectService;

    @Test
    void shouldCreateProjectSuccessfully() throws Exception {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setProjectCode("PRJ-2026-TEST");
        request.setName("Ledger Modernization");
        request.setClientName("Apex Financial");
        request.setPricingModel(PricingModel.FIXED_PRICE);
        request.setTotalContractValue(BigDecimal.valueOf(150000.00));

        ProjectResponse response = new ProjectResponse();
        response.setId(1L);
        response.setProjectCode("PRJ-2026-TEST");
        response.setName("Ledger Modernization");
        response.setClientName("Apex Financial");
        response.setPricingModel(PricingModel.FIXED_PRICE);
        response.setStatus(ProjectStatus.ACTIVE);
        response.setTotalContractValue(BigDecimal.valueOf(150000.00));

        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.projectCode").value("PRJ-2026-TEST"))
                .andExpect(jsonPath("$.pricingModel").value("FIXED_PRICE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldGetProjectById() throws Exception {
        ProjectResponse response = new ProjectResponse();
        response.setId(1L);
        response.setProjectCode("PRJ-2026-TEST");
        response.setName("Ledger Modernization");
        response.setPricingModel(PricingModel.FIXED_PRICE);

        when(projectService.getProjectById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.projectCode").value("PRJ-2026-TEST"));
    }
}
