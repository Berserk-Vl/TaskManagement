package ru.sb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.sb.config.openapi.OpenAPIConfig;
import ru.sb.service.UserService;

import java.util.Map;

@Tag(name = "users")
@RestController
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "User login.",
            description = "Login with email and password, if authentication is successful, JWT is returned.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenAPIConfig.UserSchema.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Successful authentication.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.TokenSchema.class)
                            )),
                    @ApiResponse(
                            description = "The Email and Password fields are required and cannot be null.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Authentication failed.",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Unpredicted error.",
                            responseCode = "500",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
            }
    )
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