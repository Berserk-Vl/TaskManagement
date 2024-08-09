package ru.sb.service;

import java.util.Map;

public interface TaskService {
    Map<String, Object> addTask(Map<String, String> fields);
}
