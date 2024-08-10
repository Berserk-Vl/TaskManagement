package ru.sb.service;

import org.springframework.stereotype.Service;
import ru.sb.model.Task;
import ru.sb.model.TaskRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskServiceImpl implements TaskService {
    private TaskRepository taskRepository;
    private UserService userService;
    private CommentService commentService;
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final int MAX_EMAIL_LENGTH = 30;


    public TaskServiceImpl(TaskRepository taskRepository, UserService userService, CommentService commentService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.commentService = commentService;
    }

    @Override
    public Map<String, Object> addTask(Map<String, String> fields) {
        String error;
        try {
            Task task = new Task();
            setTextField(task, fields, "author", MAX_EMAIL_LENGTH, false, true);
            setTextField(task, fields, "title", MAX_TITLE_LENGTH, false, true);
            setTextField(task, fields, "description", MAX_DESCRIPTION_LENGTH, false, true);
            setEnumField(task, fields, "status", Task.Status.values(), false, false);
            setEnumField(task, fields, "priority", Task.Priority.values(), false, false);
            setTextField(task, fields, "performer", MAX_EMAIL_LENGTH, true, false);
            return Map.of("task", taskRepository.save(task));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                error = e.getMessage();
            } else {
                error = e.getClass().toString();
            }
        }
        return Map.of("message", error);
    }

    @Override
    public Map<String, Object> updateTask(Long taskId, Map<String, String> fields) {
        String error;
        try {
            Task task = getTask(taskId);
            isAuthor(task, fields);
            setTextField(task, fields, "title", MAX_TITLE_LENGTH, false, false);
            setTextField(task, fields, "description", MAX_DESCRIPTION_LENGTH, false, false);
            setEnumField(task, fields, "status", Task.Status.values(), false, false);
            setEnumField(task, fields, "priority", Task.Priority.values(), false, false);
            setTextField(task, fields, "performer", MAX_EMAIL_LENGTH, true, false);
            return Map.of("task", taskRepository.save(task));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                error = e.getMessage();
            } else {
                error = e.getClass().toString();
            }
        }
        return Map.of("message", error);
    }

    @Override
    public Map<String, Object> getTasks(Map<String, String> filters) {
        String error;
        try {
            String author = getStringFilterValue(filters, "author");
            String performer = getStringFilterValue(filters, "performer");
            long offset = getLongFilterValue(filters, "offset");
            long limit = getLongFilterValue(filters, "limit");
            if (offset > 0 && limit == 0) {
                throw new IllegalArgumentException("ERROR: For an offset value > 0 need to provide a limit value > 0.");
            }
            boolean comments = getBooleanFilterValue(filters, "comments");
            Stream<Task> taskStream = taskRepository.findAll().stream();
            if (author != null && !author.equals("")) {
                taskStream = taskStream.filter(task -> task.getAuthor().equals(author));
            } else if (author == null) {
                throw new IllegalArgumentException("ERROR: Filter(author) can't be null.");
            }
            if (!"".equals(performer)) {
                taskStream = taskStream.filter(task -> {
                    if (task.getPerformer() == null) {
                        return task.getPerformer() == null && performer == null;
                    }
                    return task.getPerformer().equals(performer);
                });
            }
            List<Task> taskList = taskStream.toList();
            long totalFilteredTask = taskList.size();
            taskStream = taskList.stream();
            if (offset * limit >= totalFilteredTask) {
                throw new IllegalArgumentException(
                        String.format("ERROR: You wanted to skip %d, but after filtering there were only %d items left.",
                                offset * limit, totalFilteredTask));
            }
            if (limit != 0) {
                taskStream = taskStream.skip(offset * limit).limit(limit);
            }
            Object tasksObject = comments ? taskStream
                    .map(task -> Map.of("task", task, "comments", commentService.findAllByTaskId(task.getId())))
                    .collect(Collectors.toList()) : taskStream.toList();
            return Map.of("tasks", tasksObject, "total", totalFilteredTask);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                error = e.getMessage();
            } else {
                error = e.getClass().toString();
            }
        }
        return Map.of("message", error);
    }

    @Override
    public Map<String, Object> deleteTask(Long taskId, String requester) {
        String error;
        try {
            Task task = getTask(taskId);
            isAuthor(task, Map.of("author", requester));
            taskRepository.deleteById(taskId);
            return Map.of("task", task);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                error = e.getMessage();
            } else {
                error = e.getClass().toString();
            }
        }
        return Map.of("message", error);
    }

    @Override
    public Map<String, Object> setTaskStatus(Long taskId, Map<String, String> fields) {
        String error;
        try {
            Task task = getTask(taskId);
            if (fields.containsKey("requester") && fields.get("requester") != null) {
                if (task.getAuthor().equals(fields.get("requester"))
                        || fields.get("requester").equals(task.getPerformer())) {
                    setEnumField(task, fields, "status", Task.Status.values(), false, true);
                    return Map.of("task", taskRepository.save(task));
                } else {
                    error = String.format("ERROR: You are not an author or a performer of the task(%d).", taskId);
                }
            } else {
                error = "ERROR: Can't identify requester.";
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                error = e.getMessage();
            } else {
                error = e.getClass().toString();
            }
        }
        return Map.of("message", error);
    }

    @Override
    public Map<String, Object> setTaskPerformer(Long taskId, Map<String, String> fields) {
        String error;
        try {
            Task task = getTask(taskId);
            isAuthor(task, fields);
            setTextField(task, fields, "performer", MAX_EMAIL_LENGTH, true, true);
            return Map.of("task", taskRepository.save(task));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("ERROR")) {
                error = e.getMessage();
            } else {
                error = e.getClass().toString();
            }
        }
        return Map.of("message", error);
    }

    private Task getTask(Long taskId) throws NoSuchElementException {
        if (taskRepository.findById(taskId).isPresent()) {
            return taskRepository.findById(taskId).get();
        }
        throw new NoSuchElementException(String.format("ERROR: A task(%d) not exists.", taskId));
    }

    private void isAuthor(Task task, Map<String, String> fields) throws IllegalArgumentException {
        if (!(fields.containsKey("author") && task.getAuthor().equals(fields.get("author")))) {
            throw new IllegalArgumentException(String.format("ERROR: You are not an author of the task(%d).", task.getId()));
        }
    }

    private void setTextField(Task task, Map<String, String> fields, String fieldName,
                              int maxLength, boolean nullable, boolean required)
            throws NullPointerException, NoSuchElementException, IllegalArgumentException {
        if (fields.containsKey(fieldName)) {
            if (fields.get(fieldName) != null) {
                if (fields.get(fieldName).length() <= maxLength) {
                    switch (fieldName) {
                        case "title" -> task.setTitle(fields.get(fieldName));
                        case "description" -> task.setDescription(fields.get(fieldName));
                        case "author" -> task.setAuthor(fields.get(fieldName));
                        case "performer" -> {
                            if (userService.findUserByEmail(fields.get("performer")) != null) {
                                task.setPerformer(fields.get(fieldName));
                            } else {
                                throw new IllegalArgumentException(
                                        String.format("ERROR: Field(%s) can't be set, because user with specified email(%s) not exists.",
                                                fieldName, fields.get(fieldName)));
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException(String.format("ERROR: Field(%s) exceeds max length(%d > %d).",
                            fieldName, fields.get(fieldName).length(), maxLength));
                }
            } else if (nullable) {
                switch (fieldName) {
                    case "performer" -> task.setPerformer(fields.get(fieldName));
                }
            } else {
                throw new NullPointerException(String.format("ERROR: Field(%s) can not be null.", fieldName));
            }
        } else if (required) {
            throw new NoSuchElementException(String.format("ERROR: Field(%s) not found.", fieldName));
        }
    }

    private void setEnumField(Task task, Map<String, String> fields, String fieldName,
                              Object[] validValues, boolean nullable, boolean required)
            throws NullPointerException, NoSuchElementException, IllegalArgumentException {
        if (fields.containsKey(fieldName)) {
            if (fields.get(fieldName) != null) {
                try {
                    switch (fieldName) {
                        case "status" -> task.setStatus(Task.Status.valueOf(fields.get(fieldName).toUpperCase()));
                        case "priority" -> task.setPriority(Task.Priority.valueOf(fields.get(fieldName).toUpperCase()));
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(String.format("ERROR: Invalid value for %s, valid values are %s.",
                            fieldName, Arrays.toString(validValues)));
                }
            } else if (!nullable) {
                throw new NullPointerException(String.format("ERROR: Field(%s) can not be null.", fieldName));
            }

        } else if (required) {
            throw new NoSuchElementException(String.format("ERROR: Field(%s) not found.", fieldName));
        }
    }

    private String getStringFilterValue(Map<String, String> filters, String filterName)
            throws IllegalArgumentException {
        if (filters.containsKey(filterName)) {
            if (filters.get(filterName).equals("ME")) {
                if (filters.containsKey("requester") && filters.get("requester") != null) {
                    return filters.get("requester");
                } else {
                    throw new IllegalArgumentException(
                            String.format("ERROR: Can't identify the filter(%s) value.", filterName));
                }
            } else {
                return filters.get(filterName);
            }
        }
        return "";
    }

    private Long getLongFilterValue(Map<String, String> filters, String filterName)
            throws NumberFormatException, IllegalArgumentException {
        if (filters.containsKey(filterName)) {
            try {
                long value = Long.parseLong(filters.get(filterName));
                if (value < 0) {
                    throw new IllegalArgumentException(String.format("ERROR: Filter(%s) can't have a negative value.", filterName));
                }
                return value;
            } catch (NumberFormatException e) {
                throw new NumberFormatException(String.format("ERROR: Filter(%s) is not Long type.", filterName));
            }
        }
        return 0L;
    }

    private Boolean getBooleanFilterValue(Map<String, String> filters, String filterName)
            throws IllegalArgumentException {
        if (filters.containsKey(filterName)) {
            if (filters.get(filterName).equalsIgnoreCase("true")) {
                return true;
            } else if (filters.get(filterName).equalsIgnoreCase("false")) {
                return false;
            }
            throw new IllegalArgumentException(String.format("ERROR: Filter(%s) is not Boolean type.", filterName));
        }
        return false;
    }
}
