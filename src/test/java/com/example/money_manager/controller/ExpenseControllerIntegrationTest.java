package com.example.money_manager.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;


@SpringBootTest
@AutoConfigureMockMvc
public class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void mustCreateExpenseTroughEndpoint() throws Exception{
        String requestBody = """
            {
                "description": "Mercado",
                "amount": 150.50,
                "date": "2026-09-05",
                "category": "ALIMENTACAO"
            }
            """;

        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Mercado"))
                .andExpect(jsonPath("$.id").exists()
                );
    }

    @Test
    void mustListEveryExpense() throws Exception{

        MvcResult beforeResult = mockMvc.perform(get("/api/expenses"))
                                        .andReturn();

        String beforeBody = beforeResult.getResponse().getContentAsString();

        int sizeBefore = objectMapper.readTree(beforeBody).size();

        String requestBody = """
            {
                "description": "Mercado",
                "amount": 150.50,
                "date": "2026-09-05",
                "category": "ALIMENTACAO"
            }
            """;


        mockMvc.perform(post("/api/expenses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // 3. Verifica que agora tem exatamente UMA a mais
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(sizeBefore + 1));

    }

    @Test
    void mustFindById() throws Exception {
        String requestBody = """
        {
            "description": "Mercado",
            "amount": 150.50,
            "date": "2026-09-05",
            "category": "ALIMENTACAO"
        }
        """;

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long createdId = objectMapper.readTree(createResponseBody).get("id").asLong();

        MvcResult mvcResult = mockMvc.perform(get("/api/expenses/" + createdId))
                                     .andExpect(status().isOk())
                                     .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertTrue(responseBody.contains("Mercado"));
    }

    @Test
    void deveRetornar404QuandoDespesaNaoExiste() throws Exception {
        mockMvc.perform(get("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }


    @Test
    void mustUpdateSuccessfully() throws Exception{
        String requestBody = """
            {
                "description": "Mercado",
                "amount": 150.50,
                "date": "2026-09-05",
                "category": "ALIMENTACAO"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestBody))
                                        .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long createdId = objectMapper.readTree(createResponseBody).get("id").asLong();


        String changeBody = """
            {
                "description": "Mudado",
                "amount": 150.50,
                "date": "2026-09-05",
                "category": "ALIMENTACAO"
            }
            """;

        MvcResult mvcResult = mockMvc.perform(put("/api/expenses/" + createdId)
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(changeBody))
                                     .andExpect(status().isOk())
                                     .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        assertTrue(responseBody.contains("Mudado"));
    }


    @Test
    void mustDeleteExpense() throws Exception{
        String requestBody = """
            {
                "description": "Mercado",
                "amount": 150.50,
                "date": "2026-09-05",
                "category": "ALIMENTACAO"
            }
            """;

        MvcResult createResult = mockMvc.perform(post("/api/expenses")
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(requestBody))
                                     .andReturn();

        String createResponseBody = createResult.getResponse().getContentAsString();
        Long createdId = objectMapper.readTree(createResponseBody).get("id").asLong();

        mockMvc.perform(delete("/api/expenses/" + createdId))
                .andExpect(status().isNoContent());

    }
}
