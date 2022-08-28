package com.example.httprecord;

import java.math.BigDecimal;

public record ResponseType(BigDecimal amount, String currency) {
}