package com.example.service;

import com.example.exception.TransactionException;
import com.example.httprecord.RequestType;
import com.example.httprecord.ResponseType;
import com.example.model.CommissionData;
import com.example.model.CommissionType;
import com.example.repository.CommissionsRepository;
import com.example.util.CommissionApplied;
import com.example.util.DateFormatConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

import static com.example.util.CommissionApplied.*;
import static com.example.util.CommonUtils.formatDate;

@Slf4j
@org.springframework.stereotype.Service
public class TransactionService {
    @Value("${transaction.currency}")
    private String EUR;

    @Value("${transaction.xrates.url}")
    private String ratesURL;

    @Value("${transaction.messages.error.xrate-failure}")
    private String xrateFailureMsg;

    @Value("${transaction.messages.error.default}")
    private String defaultErrMsg;

    @Value("${transaction.messages.error.currency-not-found}")
    private String currencyNotFound;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommissionsRepository commissionsRepository;

    @Transactional
    public ResponseEntity<Object> addTransaction(RequestType requestBody) {
        log.debug("Inside addTransaction method..");
        try {
            Date requestedDate = requestBody.date();
            String dateYearMonthDay = formatDate(DateFormatConstant.yyyyMMdd, requestedDate);

            // get rates
            ResponseEntity<Object> responseEntity = restTemplate.exchange(ratesURL + dateYearMonthDay, HttpMethod.GET, HttpEntity.EMPTY, Object.class);

            if (!responseEntity.getStatusCode().equals(HttpStatus.OK))
                return new ResponseEntity<>(xrateFailureMsg, HttpStatus.INTERNAL_SERVER_ERROR);
            else {
                String thisTransactionCurrency = requestBody.currency();
                BigDecimal thisTransactionAmount = requestBody.amount();
                int clientId = requestBody.client_id();

                //Convert amount to EUR
                BigDecimal thisTransactionAmountInEUR = convertToEUR(responseEntity, thisTransactionCurrency, thisTransactionAmount, clientId);

                BigDecimal resultCommission = getResultCommission(requestBody, clientId, thisTransactionAmountInEUR);

                //Save transaction to db
                persistTransaction(requestBody, requestedDate, clientId, thisTransactionAmountInEUR);

                return new ResponseEntity<>(new ResponseType(resultCommission, EUR), HttpStatus.CREATED);
            }
        } catch (TransactionException e) {
            return new ResponseEntity<>(defaultErrMsg + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(defaultErrMsg + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public BigDecimal convertToEUR(ResponseEntity<Object> responseEntity, String thisTransactionCurrency,
                                   BigDecimal thisTransactionAmount, int clientId) throws TransactionException {
        if (!thisTransactionCurrency.equals(EUR)) {
            //get currency exchange rate from response
            Object currencyRateObject = ((Map) ((Map) responseEntity.getBody()).get("rates")).get(thisTransactionCurrency);

            if (currencyRateObject == null) throw new TransactionException(defaultErrMsg + currencyNotFound);
            else {
                BigDecimal currencyRate = null;
                if (currencyRateObject instanceof Double currencyRateDouble)
                    currencyRate = BigDecimal.valueOf(currencyRateDouble.doubleValue());
                else if (currencyRateObject instanceof BigDecimal currencyRateBigDecimal)
                    currencyRate = currencyRateBigDecimal;

                // convert thisTransactionAmount to EUR with 2 DECIMAL
                BigDecimal thisTransactionAmountInEUR = thisTransactionAmount.divide(currencyRate, 2, RoundingMode.CEILING);
                log.info("Client client_id {} converted {} {} to {} {}", clientId, thisTransactionAmount, thisTransactionCurrency, thisTransactionAmountInEUR, EUR);

                return thisTransactionAmountInEUR;
            }
        } else return thisTransactionAmount;
    }

    private BigDecimal getResultCommission(RequestType requestBody, int clientId, BigDecimal thisTransactionAmountInEUR) throws TransactionException {
        //Get monthly turnover for the client from database
        boolean monthlyTurnoverOf1000Reached = false;
        BigDecimal clientSumOfTurnoverPerMonth = commissionsRepository.getClientSumOfTurnoverPerMonth(requestBody.client_id(),
                formatDate(DateFormatConstant.yyyyMMd, requestBody.date()));
        if (clientSumOfTurnoverPerMonth != null)
            monthlyTurnoverOf1000Reached = clientSumOfTurnoverPerMonth.compareTo(BigDecimal.valueOf(1000)) >= 0;

        // CALCULATE COMMISSIONS
        BigDecimal[] rulesCommissionArray = new BigDecimal[3];
        CommissionData commissionDataRule1 = CommissionData.builder()
                .commissionApplied(RULE1).
                requestType(requestBody).
                monthlyTurnoverOf1000Reached(monthlyTurnoverOf1000Reached).
                clientSumOfTurnoverPerMonth(clientSumOfTurnoverPerMonth).
                ThisTransactionAmountInEUR(thisTransactionAmountInEUR).build();

        // Rule #1: Default pricing
        rulesCommissionArray[0] = getRuleCommission(commissionDataRule1);

        // Rule #2: Client with a discount
        CommissionData commissionDataRule2 = commissionDataRule1.toBuilder().commissionApplied(RULE2).build();
        rulesCommissionArray[1]  = getRuleCommission(commissionDataRule2);

        // Rule #3: High turnover discount
        CommissionData commissionDataRule3 = commissionDataRule1.toBuilder().commissionApplied(RULE3).build();
        rulesCommissionArray[2] = getRuleCommission(commissionDataRule3);

        // APPLY COMMISSIONS RULES
        BigDecimal resultCommission = calculateCommissionAndRule(clientId, rulesCommissionArray, monthlyTurnoverOf1000Reached);
        return resultCommission;
    }

    public BigDecimal getRuleCommission(CommissionData commissionData) throws TransactionException {
        try {
            BigDecimal commission = BigDecimal.ZERO;
            switch (commissionData.getCommissionApplied()) {
                case RULE1: {
                    commission = commissionData.getThisTransactionAmountInEUR().multiply(BigDecimal.valueOf(0.005));
                    if (commission.compareTo(BigDecimal.valueOf(0.05)) == -1) commission = BigDecimal.valueOf(0.05);
                    break;
                }
                case RULE2: {
                    if (commissionData.getRequestType().client_id() == 42) commission = BigDecimal.valueOf(0.05);
                    else commission = BigDecimal.ZERO;
                    break;
                }
                case RULE3: {
                    commission = BigDecimal.ZERO;
                    if (commissionData.isMonthlyTurnoverOf1000Reached()) {
                        // commission is = 0,03 EUR for the current transaction
                        commission = BigDecimal.valueOf(0.03);
                        log.info("Client client_id {} processed monthly turnover was {}", commissionData.getRequestType().client_id(),
                                commissionData.getClientSumOfTurnoverPerMonth());
                    } else
                        log.info("Client client_id {} processed monthly turnover was {} {}", commissionData.getRequestType().client_id(), BigDecimal.ZERO, EUR);
                    break;
                }
            }
            return commission;
        } catch (Exception e) {
            throw new TransactionException(e.getMessage(), e.getCause());
        }
    }

    private BigDecimal calculateCommissionAndRule(int clientId, BigDecimal[] ruleCommissionArr, boolean monthlyTurnoverOf1000EUROHasBeenReached) {
        BigDecimal resultCommission = ruleCommissionArr[0];
        CommissionApplied commissionAppliedRule = CommissionApplied.RULE1;
        if (clientId == 42) {
            if (!monthlyTurnoverOf1000EUROHasBeenReached) {
                resultCommission = ruleCommissionArr[1];
                commissionAppliedRule = RULE2;
            } else {
                if (ruleCommissionArr[0].compareTo(ruleCommissionArr[2]) == -1) {
                    resultCommission = ruleCommissionArr[0];
                    commissionAppliedRule = RULE1;
                } else {
                    resultCommission = ruleCommissionArr[2];
                    commissionAppliedRule = RULE3;
                }
            }
        } else if (monthlyTurnoverOf1000EUROHasBeenReached) {
            resultCommission = ruleCommissionArr[2];
            commissionAppliedRule = RULE3;
        }
        resultCommission = resultCommission.setScale(2, RoundingMode.CEILING);

        log.info("Client client_id {} applied commision {} = {} EUR", clientId, commissionAppliedRule.toString(), resultCommission);
        return resultCommission;
    }

    private void persistTransaction(RequestType requestBody, Date requestedDate, int clientId, BigDecimal thisTransactionAmountInEUR) {
        // get current date CommissionType
        CommissionType commissionType = commissionsRepository.findByIdAndDate(clientId, requestedDate);
        if (commissionType == null)
            commissionType = new CommissionType(0, clientId, requestBody.date(), thisTransactionAmountInEUR);
        else {
            // get processed daily turnover add this transaction amount and save to db
            commissionType.setAmount(commissionType.getAmount().add(thisTransactionAmountInEUR));
        }
        commissionsRepository.save(commissionType);
    }
}