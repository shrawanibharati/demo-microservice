package com.example.controller;

import com.example.httprecord.transaction.TransactionRequestType;
import com.example.httprecord.transaction.TransactionResponseType;
import com.example.service.impl.TransactionCrudOperationServiceImpl;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@Api(value = "Transaction microservice")
public class TransactionController {

    final TransactionCrudOperationServiceImpl service;

    public TransactionController(TransactionCrudOperationServiceImpl service) {
        this.service = service;
    }

    @ApiOperation("Add transaction")
    @PostMapping(value = "/transaction")
    @HystrixCommand(fallbackMethod = "fallbackTransaction", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000")})
    public @ResponseBody ResponseEntity<Object> addTransaction(@RequestBody TransactionRequestType body) {
        return service.create(body);
    }

    public @ResponseBody ResponseEntity<Object> fallbackTransaction(TransactionRequestType body) {
        return new ResponseEntity<>("Fallback method for transaction API..", HttpStatus.CREATED);
    }
}
