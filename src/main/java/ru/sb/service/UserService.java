package ru.sb.service;

import ru.sb.model.User;

import java.util.Map;

public interface UserService {
    User findUserByEmail(String email);

    Map<String, Object> login(Map<String, String> fields);
}