package com.example.service;

import com.example.httprecord.transaction.TransactionRequestType;
import com.example.httprecord.transaction.TransactionResponseType;
import com.example.model.Transaction;
import com.example.repository.TransactionsRepository;
import com.example.service.impl.TransactionCrudOperationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@WebMvcTest(TransactionCrudOperationServiceImpl.class)
class TransactionCrudOperationServiceImplTest {

	@MockBean
	private RestTemplate restTemplate;

	@Autowired
	private TransactionCrudOperationServiceImpl service;

	@MockBean
	private TransactionsRepository transactionsRepository;

	private final String amountCurrency = "PLN";

	static Date requestedDate;
	static String dateYearMonth;
	private final Map responseBodyMap;

	TransactionCrudOperationServiceImplTest() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream("SampleExchangeRateResponse.json");
		ObjectMapper mapper = new ObjectMapper();
		responseBodyMap = mapper.readValue(is, Map.class);
	}

	@BeforeAll
	static void beforeAll(){
		requestedDate = new Date(2021,1,1);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
		dateYearMonth = simpleDateFormat.format(requestedDate);
	}

	@Test
	void ClientSumOfTurnoverPerMonthNotMoreThan1000() {

		testTransactionService(123, 500, 50, 100, 21.88);
	}

	@Test
	void ClientSumOfTurnoverPerMonthMoreThan100() {
		testTransactionService(42, 1000, 0, 100, 22.98);
	}

	private void testTransactionService(int clientId, int consumedTurnoverPerMonth, int consumedTurnoverForCurrentDay, int currentTurnover, double expectedCommission) {
		Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), Mockito.any(Class.class))).thenReturn(new ResponseEntity <Object>(responseBodyMap, HttpStatus.OK));
		Mockito.when(transactionsRepository.getClientSumOfTurnoverPerMonth(Mockito.anyInt(), Mockito.anyString())).thenReturn(BigDecimal.valueOf(consumedTurnoverPerMonth));
		Mockito.when(transactionsRepository.findByIdAndDate(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(new Transaction(0, clientId, requestedDate, BigDecimal.valueOf(consumedTurnoverForCurrentDay)));

		ResponseEntity<?> responseEntity = service.create(new TransactionRequestType(requestedDate, BigDecimal.valueOf(currentTurnover), amountCurrency, clientId));

		Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Assertions.assertEquals(BigDecimal.valueOf(expectedCommission), ((TransactionResponseType)responseEntity.getBody()).amount());
	}

}

