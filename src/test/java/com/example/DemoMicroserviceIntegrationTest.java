
package com.example;

import com.example.httprecord.house.HouseRequestType;
import com.example.httprecord.house.HouseResponseType;
import com.example.httprecord.transaction.TransactionRequestType;
import com.example.httprecord.transaction.TransactionResponseType;
import com.example.model.House;
import com.example.repository.HouseRepository;
import com.example.repository.TransactionsRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class DemoMicroserviceIntegrationTests
//		extends AbstractIntegrationTests
{

	@Autowired
	TestRestTemplate testRestTemplate;

	@LocalServerPort
	int port;

	private static Calendar calendar = Calendar.getInstance();

	@Autowired
	TransactionsRepository transactionsRepository;

	@Autowired
	HouseRepository houseRepository;

	String amountCurrency = "PLN";
	String url;

	@BeforeAll
	static void beforeAll(){
		calendar.set(Calendar.YEAR, 2021);
		calendar.set(Calendar.MONTH, 1);
		calendar.set(Calendar.DATE, 1);
	}

	//@Test
	void integrationTestsForTransaction() throws InterruptedException {

		transactionsRepository.deleteAll();
		url = "http://localhost:" + port + "/transaction";

		try {
			//Request Type : Rule 1 on 1st Client
			testTransactionRequest(10000, 1, 2327.92);

			//Request Type : Rule 1 on 2nd Client
			testTransactionRequest(200, 2, 44.35);

		}catch (Exception e){
			Assertions.fail();
			transactionsRepository.deleteAll();
			e.printStackTrace();
		}
		transactionsRepository.deleteAll();
	}


	private void testTransactionRequest(int val, int client_id, double val1) {
		TransactionRequestType requestTypeRule1Apply1stClient = new TransactionRequestType(calendar.getTime(), BigDecimal.valueOf(val), amountCurrency, client_id);
		HttpEntity<TransactionRequestType> httpEntityRule1Apply1stClient = new HttpEntity<>(requestTypeRule1Apply1stClient);
		ResponseEntity<TransactionResponseType> responseEntityRule1Apply1stClient = testRestTemplate.exchange(url, HttpMethod.POST, httpEntityRule1Apply1stClient, TransactionResponseType.class);
		Assertions.assertEquals(HttpStatus.CREATED, responseEntityRule1Apply1stClient.getStatusCode());
		Assertions.assertEquals(BigDecimal.valueOf(val1), responseEntityRule1Apply1stClient.getBody().amount());
	}

	@Test
	void integrationTestsForHouse() throws InterruptedException {

		houseRepository.deleteAll();
		url = "http://localhost:" + port + "/house";

		try {
			HouseRequestType houseRequestType = new HouseRequestType(calendar.getTime(), BigDecimal.valueOf(1000), amountCurrency,0);
			//test post
			HttpEntity<HouseRequestType> httpEntityRule1Apply1stClient = new HttpEntity<>(houseRequestType);
			ResponseEntity<Object> responseEntityPost = testRestTemplate.exchange(url, HttpMethod.POST,
					httpEntityRule1Apply1stClient, Object.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityPost.getStatusCode());
			int idcreated = Integer.valueOf(responseEntityPost.getBody().toString());

			//test get
			ResponseEntity<Object> responseEntityGet = testRestTemplate.exchange(url+"/"+idcreated, HttpMethod.GET,
					null, Object.class);
			Assertions.assertEquals(HttpStatus.OK, responseEntityGet.getStatusCode());

			//test get all
			ResponseEntity<Object> responseEntityGetAll = testRestTemplate.exchange(url, HttpMethod.GET,
					null, Object.class);
			Assertions.assertEquals(HttpStatus.OK, responseEntityGetAll.getStatusCode());

			//test sort by amount
			ResponseEntity<Object> responseEntitySortByAmount = testRestTemplate.exchange(url+"/sortbyamount", HttpMethod.GET,
					null, Object.class);
			Assertions.assertEquals(HttpStatus.OK, responseEntitySortByAmount.getStatusCode());

			//test put
			HouseRequestType houseRequestTypePut =  houseRequestType.builder().amount(BigDecimal.valueOf(2000)).id(idcreated).build();
			ResponseEntity<Object> responseEntityPut = testRestTemplate.exchange(url+"/"+idcreated, HttpMethod.PUT,
					new HttpEntity<>(houseRequestTypePut), Object.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntityPut.getStatusCode());

			//test delete
			ResponseEntity<Object> responseEntityDelete = testRestTemplate.exchange(url+"/"+idcreated, HttpMethod.DELETE,
					null, Object.class);
			Assertions.assertEquals(HttpStatus.OK, responseEntityDelete.getStatusCode());

			List<HouseRequestType> houses = Arrays.asList(houseRequestType, houseRequestType);
			HttpEntity<List> httpEntitySaveAll = new HttpEntity<>(houses);
			ResponseEntity<Object> responseEntitySaveAll = testRestTemplate.exchange(url+"/all", HttpMethod.POST,
					httpEntitySaveAll, Object.class);
			Assertions.assertEquals(HttpStatus.CREATED, responseEntitySaveAll.getStatusCode());


		}catch (Exception e){
			Assertions.fail();
			houseRepository.deleteAll();
			e.printStackTrace();
		}
		houseRepository.deleteAll();
	}

}

