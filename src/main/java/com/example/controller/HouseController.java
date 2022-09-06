package com.example.controller;

import com.example.httprecord.house.HouseRequestType;
import com.example.service.impl.HouseCrudOperationServiceImpl;
import com.google.common.base.Preconditions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/house")
@Api(value = "Transaction microservice")
public class HouseController {
    
    final HouseCrudOperationServiceImpl houseOperationServiceImpl;

    public HouseController(HouseCrudOperationServiceImpl houseOperationServiceImpl) {
        this.houseOperationServiceImpl = houseOperationServiceImpl;
    }


    @PostMapping
    @ApiOperation("Create a house")
    public ResponseEntity<Object> create(@RequestBody HouseRequestType houseRequestType) {
        Preconditions.checkNotNull(houseRequestType);
        return houseOperationServiceImpl.create(houseRequestType);
    }

    @PostMapping(value = "all")
    @ApiOperation("Create all houses")
    public ResponseEntity<Object> createAll(@RequestBody List<HouseRequestType> houseRequestType) {
        return houseOperationServiceImpl.saveAll(houseRequestType);
    }

    @GetMapping
    @ApiOperation("Get all house")
    public ResponseEntity<Object> detail() {
        return houseOperationServiceImpl.getAll();
    }

    @PutMapping(value = "/{id}")
    @ApiOperation("Update house")
    public ResponseEntity<Object> update(@PathVariable( "id" ) int id, @RequestBody HouseRequestType houseRequestType) {
        Preconditions.checkNotNull(houseRequestType);
        Preconditions.checkNotNull(houseRequestType.id());
        return houseOperationServiceImpl.update(id, houseRequestType);
    }

    @DeleteMapping(value = "/{id}")
    @ApiOperation("Delete house")
    public ResponseEntity<Object> delete(@PathVariable("id") int id) {
        return houseOperationServiceImpl.delete(id);
    }

    @GetMapping(value = "/{id}")
    @ApiOperation("Get one house")
    public ResponseEntity<Object> get(@PathVariable int id) {
        return houseOperationServiceImpl.get(id);
    }

    @GetMapping(value = "/sort")
    @ApiOperation("Sort houses by amount")
    public ResponseEntity<Object> sort(@RequestParam(required = false) String column, @RequestParam(defaultValue = "asc") String order) {

        return houseOperationServiceImpl.sort(column, order);
    }

}
