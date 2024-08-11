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

    @PostMapping("/tasks")
    public ResponseEntity<Map<String, Object>> addTask(@RequestHeader Map<String, String> head,
                                                       @RequestBody Map<String, String> body) {
        return getResponse("addTask", -1L, head, body, null, HttpStatus.CREATED);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head,
                                                          @RequestBody Map<String, String> body) {
        return getResponse("updateTask", taskId, head, body, null, HttpStatus.OK);
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTasks(@RequestHeader Map<String, String> head,
                                                        @RequestParam Map<String, String> queryParameters) {
        return getResponse("getTasks", -1L, head, null, queryParameters, HttpStatus.OK);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head) {
        return getResponse("deleteTask", taskId, head, null, null, HttpStatus.OK);
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> setTaskStatus(@PathVariable(name = "taskId") Long taskId,
                                                             @RequestHeader Map<String, String> head,
                                                             @RequestBody Map<String, String> body) {
        return getResponse("setTaskStatus", taskId, head, body, null, HttpStatus.OK);
    }

    @PutMapping("/tasks/{taskId}/performer")
    public ResponseEntity<Map<String, Object>> setTaskPerformer(@PathVariable(name = "taskId") Long taskId,
                                                                @RequestHeader Map<String, String> head,
                                                                @RequestBody Map<String, String> body) {
        return getResponse("setTaskPerformer", taskId, head, body, null, HttpStatus.OK);
    }

    @PutMapping("/tasks/{taskId}/comment")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head,
                                                          @RequestBody Map<String, String> body) {
        return getResponse("addComment", taskId, head, body, null, HttpStatus.CREATED);
    }

    private ResponseEntity<Map<String, Object>> getResponse(String request, Long taskId, Map<String, String> head,
                                                            Map<String, String> body, Map<String, String> queryParameters,
                                                            HttpStatus successCode) {
        try {
            Map<String, Object> result;
            String email = jwtService.getSubject(head.get("authorization").substring("Bearer ".length()));
            switch (request) {
                case "addTask" -> {
                    body.put("author", email);
                    result = taskService.addTask(body);
                }
                case "updateTask" -> {
                    body.put("author", email);
                    result = taskService.updateTask(taskId, body);
                }
                case "getTasks" -> {
                    queryParameters.put("requester", email);
                    result = taskService.getTasks(queryParameters);
                }
                case "deleteTask" -> result = taskService.deleteTask(taskId, email);
                case "setTaskStatus" -> {
                    body.put("requester", email);
                    result = taskService.setTaskStatus(taskId, body);
                }
                case "setTaskPerformer" -> {
                    body.put("author", email);
                    result = taskService.setTaskPerformer(taskId, body);
                }
                case "addComment" -> {
                    body.put("author", email);
                    result = taskService.addComment(taskId, body);
                }
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
                errorMessage = e.getClass().toString();
            }
            return new ResponseEntity<>(Map.of("error message", errorMessage), errorHttpStatus);
        }

    }
}
