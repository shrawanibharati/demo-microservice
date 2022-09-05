package com.example.httprecord.transaction;

import java.math.BigDecimal;
import java.util.Date;

public record TransactionRequestType(Date date, BigDecimal amount, String currency, int client_id) { }
