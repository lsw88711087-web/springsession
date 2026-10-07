package com.likelion.springsession.greeting.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageRepository {

    private final List<String> messages = new ArrayList<>();

    public void save(String message) {
        messages.add(message);
    }

    public List<String> findAll() {
        return messages;
    }
}