package com.example.service;

import com.example.httprecord.house.HouseRequestType;
import com.example.model.House;
import com.example.repository.HouseRepository;
import com.example.service.impl.HouseCrudOperationServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HouseCrudOperationServiceImplTest {

    @Autowired
    private HouseCrudOperationServiceImpl service;

    @Autowired
    private HouseRepository houseRepository;

    static Date requestedDate;

    static String currency = "EUR";


    @BeforeAll
    static void beforeAll(){
        requestedDate = new Date(2021,1,1);
    }



    @Test
    public void testCreate() {
        ResponseEntity<?> responseEntity = service.create(new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42));
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    public void testGetAll() throws JsonProcessingException {
        houseRepository.deleteAll();
        createTestHouses();
        ResponseEntity<?> responseEntity = service.getAll();
        List housesDetailHttpResponse = (List) responseEntity.getBody();
        House house = (House) housesDetailHttpResponse.get(0);
        assertThat(house).isNotNull();
        assertThat(housesDetailHttpResponse).isNotEmpty().hasSize(2);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testUpdateSuccess() throws JsonProcessingException {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        ResponseEntity<?> responseEntity1  = service.create(house1);
        int id = Integer.valueOf(responseEntity1.getBody().toString());

        ResponseEntity<?> responseEntity2 = service.update(id,house1.builder()
                .amount(BigDecimal.valueOf(100000))
                .id(id)
                        .build());
        assertEquals(HttpStatus.CREATED, responseEntity2.getStatusCode());
    }

    @Test
    public void testUpdateFailure() throws JsonProcessingException {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        ResponseEntity<?> responseEntity1  = service.create(house1);
        int id = Integer.valueOf(responseEntity1.getBody().toString());
        service.delete(id);

        ResponseEntity<?> responseEntity2 = service.update(id,house1.builder()
                .amount(BigDecimal.valueOf(100000))
                .id(id)
                .build());
        assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity2.getStatusCode());
    }

    @Test
    public void testGetSuccess() throws JsonProcessingException {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        ResponseEntity<?> responseEntity1  = service.create(house1);
        ResponseEntity<?> responseEntity2 = service.get(Integer.valueOf(responseEntity1.getBody().toString()));

        assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
        assertEquals(house1.date(), ((House)responseEntity2.getBody()).getDate());
        assertEquals(house1.currency(), ((House)responseEntity2.getBody()).getCurrency());
    }

    @Test
    public void testGetFailure() throws JsonProcessingException {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        ResponseEntity<?> responseEntity1  = service.create(house1);
        int id = Integer.valueOf(responseEntity1.getBody().toString());
        service.delete(id);
        ResponseEntity<?> responseEntity2 = service.get(id);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity2.getStatusCode());
    }

    @Test
    public void testDeleteSuccess() throws JsonProcessingException {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        ResponseEntity<?> responseEntity1  = service.create(house1);
        int id = Integer.valueOf(responseEntity1.getBody().toString());

        ResponseEntity<?> responseEntity2 =  service.delete(id);

        assertEquals(HttpStatus.OK, responseEntity2.getStatusCode());
    }

    @Test
    public void testDeleteFailure() throws JsonProcessingException {
        ResponseEntity<?> responseEntity1 = createSingleHouse();
        int id = Integer.valueOf(responseEntity1.getBody().toString());
        service.delete(id);
        ResponseEntity<?> responseEntity2 =  service.delete(id);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity2.getStatusCode());
    }

    @Test
    public void testSaveAll() throws JsonProcessingException {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        HouseRequestType house2  = house1.builder()
                .amount(BigDecimal.valueOf(100000))
                .build();
        List<HouseRequestType> houses = Arrays.asList(house1,house2);
        ResponseEntity<?> responseEntity = service.saveAll(houses);
        List ids = (List) responseEntity.getBody();
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(2, ids.size());
    }

    @Test
    public void testSortByAmountAsc() throws JsonProcessingException {
        houseRepository.deleteAll();
        createTestHouses();
        ResponseEntity<?> responseEntity = service.sort("amount", "asc");
        List housesDetailHttpResponse = (List) responseEntity.getBody();
        House house = (House) housesDetailHttpResponse.get(0);
        assertThat(house).isNotNull();
        BigDecimal value = house.getAmount().stripTrailingZeros();
        if (value.scale() < 1)
            value = value.setScale(0);
        assertEquals(value, BigDecimal.valueOf(1000));
    }

    @Test
    public void testSortByAmountDesc() throws JsonProcessingException {
        houseRepository.deleteAll();
        createTestHouses();
        ResponseEntity<?> responseEntity = service.sort("amount", "desc");
        List housesDetailHttpResponse = (List) responseEntity.getBody();
        House house = (House) housesDetailHttpResponse.get(0);
        assertThat(house).isNotNull();
        BigDecimal value = house.getAmount().stripTrailingZeros();
        if (value.scale() < 1)
            value = value.setScale(0);
        assertEquals(value, BigDecimal.valueOf(100000));
    }

    @Test
    public void testSortByIdDesc() throws JsonProcessingException {
        houseRepository.deleteAll();
        createTestHouses();
        ResponseEntity<?> responseEntity = service.sort("id", "desc");
        List housesDetailHttpResponse = (List) responseEntity.getBody();
        House house1 = (House) housesDetailHttpResponse.get(0);
        House house2 = (House) housesDetailHttpResponse.get(0);
        assertThat(house1).isNotNull();
        assertThat(house2).isNotNull();
        BigDecimal value1 = house1.getAmount().stripTrailingZeros();
        BigDecimal value2 = house2.getAmount().stripTrailingZeros();
        if (value1.scale() < 1)
            value1 = value1.setScale(0);
        if (value2.scale() < 1)
            value2 = value2.setScale(0);
        assertThat(value1.compareTo(value2) > -1);
    }

    @Test
    public void testSortByIdAsc() throws JsonProcessingException {
        houseRepository.deleteAll();
        createTestHouses();
        ResponseEntity<?> responseEntity = service.sort("id", "desc");
        List housesDetailHttpResponse = (List) responseEntity.getBody();
        House house1 = (House) housesDetailHttpResponse.get(0);
        House house2 = (House) housesDetailHttpResponse.get(0);
        assertThat(house1).isNotNull();
        assertThat(house2).isNotNull();
        BigDecimal value1 = house1.getAmount().stripTrailingZeros();
        BigDecimal value2 = house2.getAmount().stripTrailingZeros();
        if (value1.scale() < 1)
            value1 = value1.setScale(0);
        if (value2.scale() < 1)
            value2 = value2.setScale(0);
        assertThat(value1.compareTo(value2) < 0);
    }

    private ResponseEntity<?> createSingleHouse() {
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        ResponseEntity<?> responseEntity1  = service.create(house1);
        return responseEntity1;
    }

    private void createTestHouses(){
        HouseRequestType house1 = new HouseRequestType(requestedDate, BigDecimal.valueOf(1000), currency, 42);
        HouseRequestType house2 =  new HouseRequestType(requestedDate, BigDecimal.valueOf(100000), currency, 42);
        service.create(house1);
        service.create(house2);
    }



}
