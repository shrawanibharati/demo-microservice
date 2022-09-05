package com.example.httprecord.house;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Date;
@Builder
public record HouseRequestType(Date date, BigDecimal amount, String currency, int id) { }
