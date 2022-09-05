package com.example.service;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CrudOperationService<T, R, S> {
    public ResponseEntity<T> create(R requestType) ;

    public default ResponseEntity<T> saveAll(List<R> requestType) {
        return null;
    }

    public default ResponseEntity<T> getAll() {
        return null;
    }

    public default ResponseEntity<T> update(int id, R requestType) {
        return null;
    }

    public default ResponseEntity<T> get(S id) {
        return null;
    }

    public default ResponseEntity<T> delete(S id) {
        return null;
    }
}
