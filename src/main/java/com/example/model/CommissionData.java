package com.example.model;

import com.example.httprecord.RequestType;
import com.example.util.CommissionApplied;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommissionData {

    CommissionApplied commissionApplied;
    RequestType requestType;
    boolean monthlyTurnoverOf1000Reached;
    BigDecimal clientSumOfTurnoverPerMonth;
    BigDecimal ThisTransactionAmountInEUR;

}
