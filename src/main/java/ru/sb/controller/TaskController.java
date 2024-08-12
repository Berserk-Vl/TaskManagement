package ru.sb.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sb.config.openapi.OpenAPIConfig;
import ru.sb.config.security.JwtService;
import ru.sb.model.Task;
import ru.sb.service.TaskService;

import java.util.Map;

@SecurityRequirement(name = "JWT", scopes = {"read", "write"})
@Tag(name = "tasks")
@RestController
public class TaskController {
    private TaskService taskService;
    @Autowired
    private JwtService jwtService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
            summary = "Add a new task.",
            description = "Allows the user to add a new task with the specified parameters.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Task data.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenAPIConfig.TaskAddSchema.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Task added.",
                            responseCode = "201",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.TaskOutputSchema.class)
                            )),
                    @ApiResponse(
                            description = "Incorrect data was provided.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Unauthenticated access.",
                            responseCode = "403"),
                    @ApiResponse(
                            description = "Unpredicted error.",
                            responseCode = "500",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
            }
    )
    @PostMapping("/tasks")
    public ResponseEntity<Map<String, Object>> addTask(@RequestHeader Map<String, String> head,
                                                       @RequestBody Map<String, String> body) {
        return getResponse("addTask", -1L, head, body, null, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update a task.",
            description = "Allows the user to update an existing task with the specified parameters.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Fields that need to be updated.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenAPIConfig.TaskUpdateSchema.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Task updated.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.TaskOutputSchema.class)
                            )),
                    @ApiResponse(
                            description = "Incorrect data was provided.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "The user is not the author of the task. | Unauthenticated access.",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Task with provided task id doesn't exist.",
                            responseCode = "404",
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
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> updateTask(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head,
                                                          @RequestBody Map<String, String> body) {
        return getResponse("updateTask", taskId, head, body, null, HttpStatus.OK);
    }

    @Operation(
            summary = "Get all tasks.",
            description = "Allows the user to get all task(with|without comments) which can be filtered or paginated.",
            parameters = {
                    @Parameter(
                            name = "author",
                            description = "Author of a task(ME as shortcut for your email).",
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "performer",
                            description = "Performer  of a task(ME as shortcut for your email).",
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "status",
                            description = "Task status.",
                            schema = @Schema(implementation = Task.Status.class)
                    ),
                    @Parameter(
                            name = "priority",
                            description = "Task priority",
                            schema = @Schema(implementation = Task.Priority.class)
                    ),
                    @Parameter(
                            name = "comments",
                            description = "Show comments of a task.",
                            schema = @Schema(implementation = Boolean.class)
                    ),
                    @Parameter(
                            name = "offset",
                            description = "Number of pages to skip.",
                            schema = @Schema(type = "integer32", minimum = "0")
                    ),
                    @Parameter(
                            name = "limit",
                            description = "Tasks per page.",
                            schema = @Schema(type = "integer32", minimum = "0")
                    ),},
            responses = {
                    @ApiResponse(
                            description = "Successful request.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    allOf = {
                                            @Schema(implementation = OpenAPIConfig.TasksSchema.class),
                                            @Schema(implementation = OpenAPIConfig.TasksCommentsSchema.class)}
                            )),
                    @ApiResponse(
                            description = "Incorrect data was provided.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "The user is not the author of the task.",
                            responseCode = "403"),
                    @ApiResponse(
                            description = "Unpredicted error.",
                            responseCode = "500",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
            }
    )
    @GetMapping("/tasks")
    public ResponseEntity<Map<String, Object>> getTasks(@RequestHeader Map<String, String> head,
                                                        @Parameter(hidden = true)
                                                        @RequestParam Map<String, String> queryParameters) {
        return getResponse("getTasks", -1L, head, null, queryParameters, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete a task.",
            description = "Allows the user to delete a task with a given taskId.",
            responses = {
                    @ApiResponse(
                            description = "Task deleted.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.TaskOutputSchema.class)
                            )),
                    @ApiResponse(
                            description = "The user is not the author of the task. | Unauthenticated access.",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Task with provided task id doesn't exist.",
                            responseCode = "404",
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
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteTask(@PathVariable(name = "taskId") Long taskId,
                                                          @RequestHeader Map<String, String> head) {
        return getResponse("deleteTask", taskId, head, null, null, HttpStatus.OK);
    }

    @Operation(
            summary = "Update a task status.",
            description = "Allows the user to update a status of an existing task with the specified parameter.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A new task status.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenAPIConfig.TaskUpdateStatusSchema.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Task status updated.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.TaskOutputSchema.class)
                            )),
                    @ApiResponse(
                            description = "Incorrect data was provided.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "The user is not the author or performer of the task. | Unauthenticated access.",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Task with provided task id doesn't exist.",
                            responseCode = "404",
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
    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<Map<String, Object>> setTaskStatus(@PathVariable(name = "taskId") Long taskId,
                                                             @RequestHeader Map<String, String> head,
                                                             @RequestBody Map<String, String> body) {
        return getResponse("setTaskStatus", taskId, head, body, null, HttpStatus.OK);
    }

    @Operation(
            summary = "Update a task performer.",
            description = "Allows the user to update a performer of an existing task with the specified parameter.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "A new task performer.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenAPIConfig.TaskUpdatePerformerSchema.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Task performer updated.",
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.TaskOutputSchema.class)
                            )),
                    @ApiResponse(
                            description = "Incorrect data was provided.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "The user is not the author of the task. | Unauthenticated access.",
                            responseCode = "403",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Task with provided task id doesn't exist.",
                            responseCode = "404",
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
    @PutMapping("/tasks/{taskId}/performer")
    public ResponseEntity<Map<String, Object>> setTaskPerformer(@PathVariable(name = "taskId") Long taskId,
                                                                @RequestHeader Map<String, String> head,
                                                                @RequestBody Map<String, String> body) {
        return getResponse("setTaskPerformer", taskId, head, body, null, HttpStatus.OK);
    }

    @Operation(
            summary = "Add a comment to a task.",
            description = "Allows the user to add a comment to a task by providing the comment text.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The comment text.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenAPIConfig.CommentAddSchema.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            description = "Comment added.",
                            responseCode = "201",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.CommentSchema.class)
                            )),
                    @ApiResponse(
                            description = "Incorrect data was provided.",
                            responseCode = "400",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OpenAPIConfig.ErrorSchema.class)
                            )),
                    @ApiResponse(
                            description = "Unauthenticated access.",
                            responseCode = "403"),
                    @ApiResponse(
                            description = "Task with provided task id doesn't exist.",
                            responseCode = "404",
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
                errorMessage = String.format("ERROR[%d]: %s.", errorHttpStatus.value(), e.getClass().toString());
            }
            return new ResponseEntity<>(Map.of("error message", errorMessage), errorHttpStatus);
        }

    }
}
