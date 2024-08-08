package ru.sb.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.sb.service.TaskService;

@RestController
public class TaskController {
    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
}
