package org.sid.bankaccountservice.entities;

import org.sid.bankaccountservice.enums.AccountType;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "p2", types = BankAccount.class)
public interface AccountProjectionP2 {
    String getId();
    Double getBalance();
    AccountType getType();
}
