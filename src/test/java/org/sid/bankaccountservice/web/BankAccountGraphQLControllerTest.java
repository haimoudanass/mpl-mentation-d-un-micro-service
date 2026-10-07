package org.sid.bankaccountservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.graphql.test.autoconfigure.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

@SpringBootTest
@AutoConfigureGraphQlTester
class BankAccountGraphQLControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void shouldListAccountsAndCustomers() {
        GraphQlTester.Response response = graphQlTester.document("""
                        query {
                          accountsList { id balance currency type }
                          customersList { id name }
                        }
                        """)
                .execute();
        response.path("accountsList").entityList(Object.class).hasSizeGreaterThan(0);
        response.path("customersList").entityList(Object.class).hasSizeGreaterThan(0);
    }

    @Test
    void shouldCreateAccountViaMutation() {
        graphQlTester.document("""
                        mutation($bankAccount: BankAccountDTO) {
                          addAccount(bankAccount: $bankAccount) {
                            id
                            balance
                            currency
                            type
                          }
                        }
                        """)
                .variable("bankAccount", java.util.Map.of(
                        "balance", 4000,
                        "currency", "USD",
                        "type", "CURRENT_ACCOUNT"
                ))
                .execute()
                .path("addAccount.id").hasValue()
                .path("addAccount.currency").entity(String.class).isEqualTo("USD");
    }

    @Test
    void shouldReturnBusinessMessageWhenAccountIsMissing() {
        graphQlTester.document("""
                        query {
                          bankAccountById(id: "unknown") { id }
                        }
                        """)
                .execute()
                .errors()
                .satisfy(errors -> org.assertj.core.api.Assertions.assertThat(errors)
                        .anyMatch(error -> error.getMessage().contains("Account unknown not found")));
    }
}
