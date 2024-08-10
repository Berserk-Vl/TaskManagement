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

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head) {
        Map<String, Object> result = taskService.deleteTask(taskId,
                jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        if (result.containsKey("task")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> setTaskStatus(@PathVariable(name = "taskId") Long taskId,
                                                             @RequestHeader Map<String, String> head,
                                                             @RequestBody Map<String, String> body) {
        body.put("requester", jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        Map<String, Object> result = taskService.setTaskStatus(taskId, body);
        if (result.containsKey("task")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/tasks/{taskId}/performer")
    public ResponseEntity<Map<String, Object>> setTaskPerformer(@PathVariable(name = "taskId") Long taskId,
                                                                @RequestHeader Map<String, String> head,
                                                                @RequestBody Map<String, String> body) {
        body.put("author", jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        Map<String, Object> result = taskService.setTaskPerformer(taskId, body);
        if (result.containsKey("task")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/tasks/{taskId}/comment")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head,
                                                          @RequestBody Map<String, String> body) {
        body.put("author", jwtService.getSubject(head.get("authorization").substring("Bearer ".length())));
        Map<String, Object> result = taskService.addComment(taskId, body);
        if (result.containsKey("comment")) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }
}
