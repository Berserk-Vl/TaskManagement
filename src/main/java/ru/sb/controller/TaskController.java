package ru.sb.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.sb.config.security.JwtService;
import ru.sb.service.TaskService;

import java.util.Map;

@RestController
public class TaskController {
    private TaskService taskService;
    @Autowired
    private JwtService jwtService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PutMapping("/tasks")
    public ResponseEntity<Map<String, Object>> addTask(@RequestHeader Map<String, String> head, @RequestBody Map<String, String> body) {
        body.put("author", jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        Map<String, Object> result = taskService.addTask(body);
        if (result.containsKey("task")) {
            result.put("task", new ObjectMapper().convertValue(result.get("task"), new TypeReference<Map<String, Object>>() {
            }));
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
}
