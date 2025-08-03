package com.eaglebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing a list of bank accounts")
public class ListBankAccountsResponse {
    
    @Schema(description = "List of bank accounts", required = true)
    @NotNull(message = "Accounts list is required")
    @Valid
    private List<BankAccountResponse> accounts;
}
