package org.sid.bankaccountservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sid.bankaccountservice.dto.BankAccountRequestDTO;
import org.sid.bankaccountservice.entities.BankAccount;
import org.sid.bankaccountservice.enums.AccountType;
import org.sid.bankaccountservice.repositories.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldListBankAccounts() throws Exception {
        mockMvc.perform(get("/api/bankAccounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThan(0)));
    }

    @Test
    void shouldCreateUpdateAndDeleteAccount() throws Exception {
        BankAccountRequestDTO request = BankAccountRequestDTO.builder()
                .balance(8000.0)
                .currency("EUR")
                .type(AccountType.SAVING_ACCOUNT)
                .build();

        String body = mockMvc.perform(post("/api/bankAccounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(get("/api/bankAccounts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        BankAccount update = BankAccount.builder()
                .currency("USD")
                .build();
        mockMvc.perform(put("/api/bankAccounts/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(8000.0));

        mockMvc.perform(delete("/api/bankAccounts/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void springDataRestShouldExposeAccountsAndProjection() throws Exception {
        String id = bankAccountRepository.findAll().get(0).getId();

        mockMvc.perform(get("/bankAccounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bankAccounts").isArray());

        mockMvc.perform(get("/bankAccounts/" + id + "?projection=p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.type").isNotEmpty());

        mockMvc.perform(get("/bankAccounts/search/byType?t=CURRENT_ACCOUNT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.bankAccounts").isArray());
    }
}
