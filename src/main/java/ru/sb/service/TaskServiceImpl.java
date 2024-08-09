package ru.sb.service;

import org.springframework.stereotype.Service;
import ru.sb.model.Task;
import ru.sb.model.TaskRepository;

import java.util.Map;

@Service
public class TaskServiceImpl implements TaskService {
    private TaskRepository taskRepository;
    private UserService userService;


    public TaskServiceImpl(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @Override
    public Map<String, Object> addTask(Map<String, String> fields) {
        try {
            Task task = new Task();
            if (fields.containsKey("author") && userService.findUserByEmail(fields.get("author")) != null) {
                task.setAuthor(fields.get("author"));
            }
            if (fields.containsKey("title")) {
                task.setTitle(fields.get("title"));
            }
            if (fields.containsKey("description")) {
                task.setDescription(fields.get("description"));
            }
            if (fields.containsKey("status")) {
                System.out.println(Task.Status.valueOf(fields.get("status").toUpperCase()));
                task.setStatus(Task.Status.valueOf(fields.get("status").toUpperCase()));
            }
            if (fields.containsKey("priority")) {
                task.setPriority(Task.Priority.valueOf(fields.get("priority").toUpperCase()));
            }
            if (fields.containsKey("performer") && userService.findUserByEmail(fields.get("performer")) != null) {
                task.setPerformer(fields.get("performer"));
            }
            return Map.of("task", taskRepository.save(task));
        } catch (Exception e) {
            return Map.of("error", "Bad value / Missing value");
        }
    }
}
