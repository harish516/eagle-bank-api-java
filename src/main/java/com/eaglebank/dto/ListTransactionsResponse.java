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
@Schema(description = "Response containing a list of transactions")
public class ListTransactionsResponse {
    
    @Schema(description = "List of transactions", required = true)
    @NotNull(message = "Transactions list is required")
    @Valid
    private List<TransactionResponse> transactions;
}
