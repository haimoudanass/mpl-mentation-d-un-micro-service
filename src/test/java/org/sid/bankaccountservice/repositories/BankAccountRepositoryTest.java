package org.sid.bankaccountservice.repositories;

import org.junit.jupiter.api.Test;
import org.sid.bankaccountservice.entities.BankAccount;
import org.sid.bankaccountservice.enums.AccountType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void shouldSaveAndFindAccountsByType() {
        BankAccount current = BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(15000.0)
                .currency("MAD")
                .type(AccountType.CURRENT_ACCOUNT)
                .build();
        BankAccount saving = BankAccount.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(new Date())
                .balance(8000.0)
                .currency("EUR")
                .type(AccountType.SAVING_ACCOUNT)
                .build();
        bankAccountRepository.save(current);
        bankAccountRepository.save(saving);

        List<BankAccount> currents = bankAccountRepository.findByType(AccountType.CURRENT_ACCOUNT);

        assertThat(bankAccountRepository.findById(current.getId())).isPresent();
        assertThat(currents)
                .extracting(BankAccount::getId)
                .contains(current.getId());
        assertThat(currents)
                .extracting(BankAccount::getType)
                .containsOnly(AccountType.CURRENT_ACCOUNT);
    }
}
