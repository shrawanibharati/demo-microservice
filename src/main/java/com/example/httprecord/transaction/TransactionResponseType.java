package com.example.httprecord.transaction;

import java.math.BigDecimal;

public record TransactionResponseType(BigDecimal amount, String currency) {
}