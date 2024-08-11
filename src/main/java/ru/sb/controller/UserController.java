package ru.sb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sb.service.UserService;

import java.util.Map;

@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return getResponse("login", null, body, null, HttpStatus.OK);
    }

    private ResponseEntity<Map<String, Object>> getResponse(String request, Map<String, String> head,
                                                            Map<String, String> body, Map<String, String> queryParameters,
                                                            HttpStatus successCode) {
        try {
            Map<String, Object> result;
            switch (request) {
                case "login" -> result = userService.login(body);
                default -> result = Map.of();

            }
            return new ResponseEntity<>(result, successCode);
        } catch (Exception e) {
            HttpStatus errorHttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            String errorMessage;
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                errorMessage = e.getMessage();
                try {
                    String prefix = "ERROR[";
                    HttpStatus parsedCode = HttpStatus.resolve(
                            Integer.parseInt(e.getMessage().substring(prefix.length(), prefix.length() + 3)));
                    if (parsedCode != null) {
                        errorHttpStatus = parsedCode;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                errorMessage = String.format("ERROR[%d]: %s.", errorHttpStatus.value(), e.getClass().toString());
            }
            return new ResponseEntity<>(Map.of("error message", errorMessage), errorHttpStatus);
        }
    }
}