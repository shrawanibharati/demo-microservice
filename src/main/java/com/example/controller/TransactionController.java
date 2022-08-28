package com.example.controller;

import com.example.httprecord.RequestType;
import com.example.httprecord.ResponseType;
import com.example.service.TransactionService;
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
@Api(value = "Commission microservice")
public class TransactionController {

    @Autowired
    TransactionService service;

    @ApiOperation("Add transaction")
    @PostMapping(value = "/transaction")
    @HystrixCommand(fallbackMethod = "fallbackTransaction", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000")})
    public @ResponseBody ResponseEntity<Object> addTransaction(@RequestBody RequestType body) {
        /*try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        return service.addTransaction(body);
    }

    @ApiOperation("Get transaction")
    @GetMapping(value = "/transaction")
    @HystrixCommand(fallbackMethod = "fallbackTransaction", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000")})
    public @ResponseBody ResponseEntity<Object> testTransaction() {
        return new ResponseEntity<>(new ResponseType(BigDecimal.valueOf(100.0), "INR"), HttpStatus.CREATED);
    }

    public @ResponseBody ResponseEntity<Object> fallbackTransaction(RequestType body) {
        return new ResponseEntity<>("Fallback method for transaction API..", HttpStatus.CREATED);
    }
}
