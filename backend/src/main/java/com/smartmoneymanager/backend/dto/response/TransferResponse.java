package com.smartmoneymanager.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private AccountResponse fromAccount;
    private AccountResponse toAccount;
}
