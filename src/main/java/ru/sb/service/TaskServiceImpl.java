package ru.sb.service;

import org.springframework.stereotype.Service;
import ru.sb.model.TaskRepository;

@Service
public class TaskServiceImpl implements TaskService {
    private TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
