package com.eaglebank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eagle-bank.bank-account")
@Data
public class BankAccountProperties {
    
    /**
     * Default sort code for new bank accounts
     */
    private String defaultSortCode = "10-10-10";
}
