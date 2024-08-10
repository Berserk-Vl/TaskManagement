package ru.sb.service;

import java.util.Map;

public interface TaskService {
    Map<String, Object> addTask(Map<String, String> fields);

    Map<String, Object> updateTask(Long taskId, Map<String, String> fields);

    Map<String, Object> getTasks(Map<String, String> filters);
}
