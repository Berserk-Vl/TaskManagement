package ru.sb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head,
                                                          @RequestBody Map<String, String> body) {
        body.put("author", jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        Map<String, Object> result = taskService.updateTask(taskId, body);
        if (result.containsKey("task")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTasks(@RequestHeader Map<String, String> head,
                                                        @RequestParam Map<String, String> queryParameters) {
        queryParameters.put("requester", jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        Map<String, Object> result = taskService.getTasks(queryParameters);
        if (result.containsKey("tasks")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
}
