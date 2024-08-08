package ru.sb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sb.model.User;
import ru.sb.service.UserService;

import java.util.Map;

@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User body) {
        System.out.println(body.getId() + " " + body.getEmail() + " " + body.getPassword());
        if (body.getEmail() == null || body.getPassword() == null) {
            return new ResponseEntity<>(Map.of("error", "Required fields(can't be null): email and password."),
                    HttpStatus.BAD_REQUEST);
        } else {
            String authenticated = userService.authenticate(body.getEmail(), body.getPassword());
            if (authenticated.equals("authenticated")) {
                return new ResponseEntity<>(Map.of("token", userService.getJwt(body.getEmail())), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Map.of("error", authenticated), HttpStatus.FORBIDDEN);
            }
        }
    }
}