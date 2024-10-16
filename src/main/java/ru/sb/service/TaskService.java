package ru.sb.service;

import java.util.Map;

public interface TaskService {
    Map<String, Object> addTask(Map<String, String> fields);

    Map<String, Object> updateTask(Long taskId, Map<String, String> fields);

    Map<String, Object> getTasks(Map<String, String> filters);

    Map<String, Object> deleteTask(Long taskId, String requester);

    Map<String, Object> setTaskStatus(Long taskId, Map<String, String> fields);

    Map<String, Object> setTaskPerformer(Long taskId, Map<String, String> fields);

    Map<String, Object> addComment(Long taskId, Map<String, String> fields);
}
