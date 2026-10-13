package com.projects.money_controll;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MoneyControllApiTests {

    private static final String PASSWORD = "s3cret-pass";
    private static final RequestPostProcessor ALICE = httpBasic("alice", PASSWORD);
    private static final RequestPostProcessor BOB = httpBasic("bob", PASSWORD);

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void registerUsers() throws Exception {
        register("alice").andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("alice"));
        register("bob").andExpect(status().isCreated());
    }

    @Test
    void rejectsUnauthenticatedAndWrongPassword() throws Exception {
        mvc.perform(get("/api/transactions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/transactions").with(httpBasic("alice", "wrong-password")))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").with(ALICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void rejectsDuplicateAndInvalidRegistration() throws Exception {
        register("alice").andExpect(status().isConflict());
        mvc.perform(json(post("/api/auth/register"), """
                        {"username": "x", "password": "short"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createsUpdatesAndDeletesTransaction() throws Exception {
        String created = mvc.perform(json(post("/api/transactions"),
                        transaction("Salary", "3000.00", "INCOME", " Work ", "2026-03-05")).with(ALICE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("work"))
                .andExpect(jsonPath("$.amount").value(3000.00))
                .andExpect(jsonPath("$.date").value("2026-03-05"))
                .andReturn().getResponse().getHeader("Location");
        String url = "/api/transactions/" + onlyTransactionId();
        assertTrue(created.endsWith(url), created);

        mvc.perform(json(put(url), transaction("Salary + bonus", "3500.00", "INCOME", "work", "2026-03-05")).with(ALICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Salary + bonus"))
                .andExpect(jsonPath("$.amount").value(3500.00));

        mvc.perform(delete(url).with(ALICE)).andExpect(status().isNoContent());
        mvc.perform(get(url).with(ALICE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void validatesTransaction() throws Exception {
        mvc.perform(json(post("/api/transactions"), transaction("", "-5", "EXPENSE", "food", "2026-03-05")).with(ALICE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.amount").exists());
        mvc.perform(json(post("/api/transactions"), transaction("Lunch", "5", "REFUND", "food", "2026-03-05")).with(ALICE))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtersSortsAndPaginates() throws Exception {
        create(ALICE, "Salary", "3000.00", "INCOME", "work", "2026-03-01");
        create(ALICE, "Groceries", "120.50", "EXPENSE", "food", "2026-03-10");
        create(ALICE, "Restaurant", "80.00", "EXPENSE", "Food", "2026-03-20");
        create(ALICE, "Rent", "900.00", "EXPENSE", "housing", "2026-04-01");

        mvc.perform(get("/api/transactions").with(ALICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.content[0].description").value("Rent"));
        mvc.perform(get("/api/transactions?category=FOOD&sort=amount,asc").with(ALICE))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].description").value("Restaurant"));
        mvc.perform(get("/api/transactions?type=EXPENSE&from=2026-03-15&to=2026-04-01&size=1&page=1").with(ALICE))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].description").value("Restaurant"));

        mvc.perform(get("/api/transactions?sort=user.passwordHash").with(ALICE)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/transactions?from=2026-04-01&to=2026-03-01").with(ALICE)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/transactions?type=nope").with(ALICE)).andExpect(status().isBadRequest());
    }

    @Test
    void summarizesPeriod() throws Exception {
        create(ALICE, "Salary", "3000.00", "INCOME", "work", "2026-03-01");
        create(ALICE, "Groceries", "120.50", "EXPENSE", "food", "2026-03-10");
        create(ALICE, "Restaurant", "80.00", "EXPENSE", "food", "2026-03-20");
        create(ALICE, "Rent", "900.00", "EXPENSE", "housing", "2026-04-01");

        mvc.perform(get("/api/summary?from=2026-03-01&to=2026-03-31").with(ALICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(3000.00))
                .andExpect(jsonPath("$.totalExpense").value(200.50))
                .andExpect(jsonPath("$.balance").value(2799.50))
                .andExpect(jsonPath("$.byCategory", hasSize(2)))
                .andExpect(jsonPath("$.byCategory[1].category").value("food"))
                .andExpect(jsonPath("$.byCategory[1].total").value(200.50));

        // No bounds: current month, which has no transactions here.
        mvc.perform(get("/api/summary").with(BOB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.byCategory", hasSize(0)));
    }

    @Test
    void usersOnlySeeTheirOwnTransactions() throws Exception {
        create(ALICE, "Salary", "3000.00", "INCOME", "work", "2026-03-01");
        String url = "/api/transactions/" + onlyTransactionId();

        mvc.perform(get("/api/transactions").with(BOB)).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get(url).with(BOB)).andExpect(status().isNotFound());
        mvc.perform(json(put(url), transaction("Hacked", "1.00", "EXPENSE", "x", "2026-03-01")).with(BOB))
                .andExpect(status().isNotFound());
        mvc.perform(delete(url).with(BOB)).andExpect(status().isNotFound());
        mvc.perform(get(url).with(ALICE)).andExpect(jsonPath("$.description").value("Salary"));
    }

    private ResultActions register(String username) throws Exception {
        return mvc.perform(json(post("/api/auth/register"), """
                {"username": "%s", "password": "%s"}""".formatted(username, PASSWORD)));
    }

    private void create(RequestPostProcessor user, String description, String amount, String type, String category,
                        String date) throws Exception {
        mvc.perform(json(post("/api/transactions"), transaction(description, amount, type, category, date)).with(user))
                .andExpect(status().isCreated());
    }

    private Integer onlyTransactionId() throws Exception {
        String body = mvc.perform(get("/api/transactions").with(ALICE)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.content[0].id");
    }

    private static String transaction(String description, String amount, String type, String category, String date) {
        return """
                {"description": "%s", "amount": %s, "type": "%s", "category": "%s", "date": "%s"}"""
                .formatted(description, amount, type, category, date);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}
