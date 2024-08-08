package ru.sb.service;

import ru.sb.model.User;

public interface UserService {
    User findUserByEmail(String email);

    String getJwt(String email);

    String authenticate(String email, String password);
}