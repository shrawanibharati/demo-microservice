package com.example.service.impl;

import com.example.httprecord.house.HouseRequestType;
import com.example.httprecord.house.HouseResponseType;
import com.example.model.House;
import com.example.repository.HouseRepository;
import com.example.service.CrudOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.util.CommonUtils.HOUSE_WITH_ID;
import static com.example.util.CommonUtils.NOT_FOUND;


@Service
@Slf4j
public class HouseCrudOperationServiceImpl implements CrudOperationService<Object, HouseRequestType, Integer> {

    final HouseRepository houseRepository;

    public HouseCrudOperationServiceImpl(HouseRepository houseRepository) {
        this.houseRepository = houseRepository;
    }


    @Override
    @Transactional
    public ResponseEntity<Object> create(HouseRequestType houseRequestType) {

        House house = houseRepository.save(new House(0, houseRequestType.currency(), houseRequestType.date(), houseRequestType.amount()));
        log.info("Created house with id " + house.getId());
        return new ResponseEntity<>(house.getId(), HttpStatus.CREATED);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> getAll() {
        List<House> houses = houseRepository.findAll();
        return new ResponseEntity<>(houses, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> update(int id, HouseRequestType houseRequestType) {
        if(houseRepository.existsById(houseRequestType.id())) {
            House house = houseRepository.save(new House(id, houseRequestType.currency(), houseRequestType.date(), houseRequestType.amount()));
            log.info("Updated house with id " + houseRequestType.id());
            return new ResponseEntity<>(house, HttpStatus.CREATED);
        }
        else return new ResponseEntity<>(new HouseResponseType(HOUSE_WITH_ID+ houseRequestType.id() + NOT_FOUND), HttpStatus.NOT_ACCEPTABLE);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> get(Integer id) {
        Optional<House> house = houseRepository.findById(id);
        if(house.isPresent())
            return new ResponseEntity<>(house.get(), HttpStatus.OK);
        else {
            log.info("id not found");
            return new ResponseEntity<>(new HouseResponseType(HOUSE_WITH_ID + id + NOT_FOUND), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> delete(Integer id) {
        if(houseRepository.existsById(id)){
            houseRepository.deleteById(id);
            return new ResponseEntity<>(id, HttpStatus.OK);
        }
        else {
            log.info("id not found");
            return new ResponseEntity<>(new HouseResponseType(HOUSE_WITH_ID + id + NOT_FOUND), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveAll(List<HouseRequestType> requestType) {
        List<House> houses =requestType.stream().map(houseRequestType -> houseRepository.save(new House(0, houseRequestType.currency(), houseRequestType.date(), houseRequestType.amount()))).collect(Collectors.toList());
        List idsCreated = houses.stream().map(a -> a.getId()).collect(Collectors.toList());
        return new ResponseEntity<Object>(idsCreated, HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<Object> sortByAmount(){
        return new ResponseEntity<>(houseRepository.findAllByOrderByAmountAsc(), HttpStatus.OK);
    }


}
