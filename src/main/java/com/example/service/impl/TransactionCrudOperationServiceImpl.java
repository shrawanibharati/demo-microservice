package com.example.service.impl;

import com.example.exception.TransactionException;
import com.example.httprecord.transaction.TransactionRequestType;
import com.example.httprecord.transaction.TransactionResponseType;
import com.example.model.Transaction;
import com.example.repository.TransactionsRepository;
import com.example.service.CrudOperationService;
import com.example.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;

import static com.example.util.CommonUtils.formatDate;

@Slf4j
@Service
public class TransactionCrudOperationServiceImpl implements CrudOperationService<Object, TransactionRequestType, Integer> {

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

    private final TransactionsRepository transactionsRepository;

    private final RestTemplate restTemplate;

    public TransactionCrudOperationServiceImpl(TransactionsRepository transactionsRepository, RestTemplate restTemplate) {
        this.transactionsRepository = transactionsRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Override
    public ResponseEntity<Object> create(TransactionRequestType requestBody) {
        log.debug("Inside addTransaction method..");
        try {

                //Convert amount to EUR
                BigDecimal thisTransactionAmountInEUR = convertToEUR(requestBody);
                log.info("converted amount is : "+thisTransactionAmountInEUR);

                if(thisTransactionAmountInEUR.compareTo(BigDecimal.valueOf(1000)) > -1)
                    thisTransactionAmountInEUR = thisTransactionAmountInEUR.add(thisTransactionAmountInEUR.multiply(BigDecimal.valueOf(0.05))).setScale(2, RoundingMode.CEILING);
                else {
                    BigDecimal clientSumOfTurnoverPerMonth = transactionsRepository.getClientSumOfTurnoverPerMonth(requestBody.client_id(),
                            formatDate(CommonUtils.YYYYMM, requestBody.date()));

                    if (clientSumOfTurnoverPerMonth != null && clientSumOfTurnoverPerMonth.compareTo(BigDecimal.valueOf(1000)) > -1)
                        thisTransactionAmountInEUR = thisTransactionAmountInEUR.add(thisTransactionAmountInEUR.multiply(BigDecimal.valueOf(0.05))).setScale(2, RoundingMode.CEILING);
                }
                //Save transaction to db
                persistTransaction(requestBody, requestBody.date(), requestBody.client_id(), thisTransactionAmountInEUR);

                return new ResponseEntity<>(new TransactionResponseType(thisTransactionAmountInEUR, EUR), HttpStatus.CREATED);

        } catch (TransactionException e) {
            return new ResponseEntity<>(defaultErrMsg + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(defaultErrMsg + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public BigDecimal convertToEUR(TransactionRequestType requestBody) throws TransactionException {

        //get xchange rate
        ResponseEntity<Object> responseEntity = restTemplate.exchange(ratesURL +
                formatDate(CommonUtils.YYYYMMDD, requestBody.date()), HttpMethod.GET, HttpEntity.EMPTY, Object.class);
        log.debug("Response from x rates url : " +responseEntity.getBody());

        if (!responseEntity.getStatusCode().equals(HttpStatus.OK))
            throw new TransactionException("Xrates not available!");

        if (!requestBody.currency().equals(EUR)) {
            //get currency exchange rate from response
            if (responseEntity != null && responseEntity.getBody() != null) {
                Map map = (Map) responseEntity.getBody();
                if(map != null && map.get("rates") != null){
                    Map rates = (Map) map.get("rates");
                    Object currencyRateObject = rates.get(requestBody.currency());

                    if (currencyRateObject == null) throw new TransactionException(defaultErrMsg + currencyNotFound);
                    else {
                        BigDecimal currencyRate = getBigDecimal(currencyRateObject);

                        // convert thisTransactionAmount to EUR with 2 DECIMAL
                        BigDecimal thisTransactionAmountInEUR = requestBody.amount().divide(currencyRate, 2, RoundingMode.CEILING);
                        log.info("Client client_id {} converted {} {} to {} {}", requestBody.client_id(), requestBody.amount(), requestBody.currency(), thisTransactionAmountInEUR, EUR);

                        return thisTransactionAmountInEUR;
                    }
                }
            }
        } else return requestBody.amount();
        throw new TransactionException("Response from xrates is null!");
    }

    private static BigDecimal getBigDecimal(Object currencyRateObject) {
        BigDecimal currencyRate = null;
        if (currencyRateObject instanceof Double currencyRateDouble)
            currencyRate = BigDecimal.valueOf(currencyRateDouble.doubleValue());
        else if (currencyRateObject instanceof BigDecimal currencyRateBigDecimal)
            currencyRate = currencyRateBigDecimal;
        return currencyRate;
    }

    @Transactional
    public void persistTransaction(TransactionRequestType requestBody, Date requestedDate, int clientId, BigDecimal thisTransactionAmountInEUR) {
        // get current date Transaction
        Transaction transaction = transactionsRepository.findByIdAndDate(clientId, requestedDate);
        if (transaction == null)
            transaction = new Transaction(0, clientId, requestBody.date(), thisTransactionAmountInEUR);
        else {
            // get processed daily turnover add this transaction amount and save to db
            transaction.setAmount(transaction.getAmount().add(thisTransactionAmountInEUR));
        }
        transactionsRepository.save(transaction);
    }
}