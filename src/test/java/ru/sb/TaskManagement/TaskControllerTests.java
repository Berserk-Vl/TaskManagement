package ru.sb.TaskManagement;

import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.util.JsonExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.sb.config.security.JwtService;
import ru.sb.model.Comment;
import ru.sb.model.CommentRepository;
import ru.sb.model.Task;
import ru.sb.model.TaskRepository;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SqlGroup(value = {
        @Sql(
                scripts = "classpath:/db/postgres/test.sql",
                executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)})
public class TaskControllerTests {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CommentRepository commentRepository;

    private static JacksonJsonProvider jsonProvider;
    private static JsonExpectationsHelper helper;
    private static String jwt;
    private static String jwtUser;

    private static final String ERROR_MESSAGE_PATH = "$.['error message']";
    private static final String AUTHOR = "admin@sb.ru";
    private static final String USER = "user@mail.ru";
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 300;
    private static final int MAX_EMAIL_LENGTH = 30;
    private static final int MAX_COMMENT_LENGTH = 300;

    private static Task defaultTask;

    @BeforeAll
    public static void initialization() {
        jsonProvider = new JacksonJsonProvider();
        helper = new JsonExpectationsHelper();
        jwt = new JwtService().generateToken(AUTHOR);
        jwtUser = new JwtService().generateToken(USER);
        defaultTask = new Task(null, "Task", "Description", Task.Status.PENDING, Task.Priority.LOW, AUTHOR, USER);
    }

    @Test
    public void addTask() throws Exception {
        Object taskMap = getTaskAsJsonObject();

        String content = mvc.perform(MockMvcRequestBuilders
                        .post("/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(taskMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Object actualTask = jsonProvider.parse(content);
        removeIdFromTask(actualTask);
        helper.assertJsonEqual(jsonProvider.toJson(putTaskInMap(taskMap)), jsonProvider.toJson(actualTask), true);
    }

    @Test
    public void addTaskNotAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(getTaskAsJsonObject())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void addTaskTitleNotProvided() throws Exception {
        Object taskJson = getTaskAsJsonObject();
        jsonProvider.removeProperty(taskJson, "title");
        taskStringFieldNotProvided("title", MockMvcRequestBuilders
                .post("/tasks"), taskJson);
    }

    @Test
    public void addTaskTitleNull() throws Exception {
        taskFieldNull("title", MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskTitleInvalidLength() throws Exception {
        taskStringFieldInvalidLength("title", MAX_TITLE_LENGTH,
                MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskDescriptionNotProvided() throws Exception {
        Object taskJson = getTaskAsJsonObject();
        jsonProvider.removeProperty(taskJson, "description");
        taskStringFieldNotProvided("description", MockMvcRequestBuilders
                .post("/tasks"), taskJson);

    }

    @Test
    public void addTaskDescriptionNull() throws Exception {
        taskFieldNull("description", MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskDescriptionInvalidLength() throws Exception {
        taskStringFieldInvalidLength("description", MAX_DESCRIPTION_LENGTH,
                MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskStatusNotProvided() throws Exception {
        addTaskEnumFieldNotProvided("status", Task.Status.PENDING.name());
    }

    @Test
    public void addTaskStatusInvalidValue() throws Exception {
        taskEnumFieldInvalidValue("status", Task.Status.values(),
                MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskStatusNull() throws Exception {
        taskFieldNull("status", MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskPriorityNotProvided() throws Exception {
        addTaskEnumFieldNotProvided("priority", Task.Priority.LOW.name());
    }

    @Test
    public void addTaskPriorityInvalidValue() throws Exception {
        taskEnumFieldInvalidValue("priority", Task.Priority.values(),
                MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskPriorityNull() throws Exception {
        taskFieldNull("priority",
                MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void addTaskPerformerNotProvided() throws Exception {
        Object taskMap = getTaskAsJsonObject();
        jsonProvider.removeProperty(taskMap, "performer");
        taskPerformerNullOrNotProvided(taskMap, taskMap,
                MockMvcRequestBuilders.post("/tasks"), "post");

    }

    @Test
    public void addTaskPerformerNull() throws Exception {
        Object taskMap = getTaskAsJsonObject();
        jsonProvider.setProperty(taskMap, "performer", null);
        taskPerformerNullOrNotProvided(taskMap, taskMap,
                MockMvcRequestBuilders.post("/tasks"), "post");
    }

    @Test
    public void addTaskPerformerInvalidValue() throws Exception {
        taskPerformerInvalidValue(MockMvcRequestBuilders.post("/tasks"), getTaskAsJsonObject());
    }

    @Test
    public void updateTask() throws Exception {
        Object taskMap = getTaskAsJsonObject();
        jsonProvider.setProperty(taskMap, "title", "T");
        jsonProvider.setProperty(taskMap, "description", "D");
        jsonProvider.setProperty(taskMap, "status", "DONE");
        jsonProvider.setProperty(taskMap, "priority", "HIGH");
        jsonProvider.setProperty(taskMap, "performer", null);

        String content = mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}", taskRepository.save(defaultTask).getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(taskMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Object actualTask = jsonProvider.parse(content);
        removeIdFromTask(actualTask);
        helper.assertJsonEqual(jsonProvider.toJson(putTaskInMap(taskMap)), jsonProvider.toJson(actualTask), true);
    }

    @Test
    public void updateTaskNotAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}", taskRepository.save(defaultTask).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(getTaskAsJsonObject())))
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateTaskNotExists() throws Exception {
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}", 0), jwt,
                getTaskAsJsonObject(), status().isNotFound(),
                String.format("ERROR[404]: A task(%d) not exists.", 0));
    }

    @Test
    public void updateTaskNotAuthor() throws Exception {
        long taskId = taskRepository.save(defaultTask).getId();
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}", taskId), jwtUser,
                getTaskAsJsonObject(), status().isForbidden(),
                String.format("ERROR[403]: You are not an author of the task(%d).", taskId));
    }

    @Test
    public void updateTaskTitleNull() throws Exception {
        taskFieldNull("title", MockMvcRequestBuilders.put("/tasks/{taskId}",
                taskRepository.save(defaultTask).getId()), jsonProvider.createMap());
    }

    @Test
    public void updateTaskTitleInvalidLength() throws Exception {
        taskStringFieldInvalidLength("title", MAX_TITLE_LENGTH, MockMvcRequestBuilders.put("/tasks/{taskId}",
                taskRepository.save(defaultTask).getId()), jsonProvider.createMap());
    }

    @Test
    public void updateTaskDescriptionNull() throws Exception {
        taskFieldNull("description",
                MockMvcRequestBuilders.put("/tasks/{taskId}", taskRepository.save(defaultTask).getId()),
                jsonProvider.createMap());
    }

    @Test
    public void updateTaskDescriptionInvalidLength() throws Exception {
        taskStringFieldInvalidLength("description", MAX_DESCRIPTION_LENGTH,
                MockMvcRequestBuilders.put("/tasks/{taskId}", taskRepository.save(defaultTask).getId()),
                jsonProvider.createMap());
    }

    @Test
    public void updateTaskPriorityInvalidValue() throws Exception {
        taskEnumFieldInvalidValue("priority", Task.Priority.values(),
                MockMvcRequestBuilders.put("/tasks/{taskId}", taskRepository.save(defaultTask).getId()),
                jsonProvider.createMap());
    }

    @Test
    public void updateTaskPriorityNull() throws Exception {
        taskFieldNull("priority",
                MockMvcRequestBuilders.put("/tasks/{taskId}", taskRepository.save(defaultTask).getId()),
                jsonProvider.createMap());
    }

    @Test
    public void updateTaskPerformerNull() throws Exception {
        Object taskMap = jsonProvider.createMap();
        jsonProvider.setProperty(taskMap, "performer", null);
        Object expectedMap = getTaskAsJsonObject();
        jsonProvider.setProperty(expectedMap, "performer", null);
        taskPerformerNullOrNotProvided(taskMap, expectedMap,
                MockMvcRequestBuilders.put("/tasks/{taskId}", taskRepository.save(defaultTask).getId()), "put");
    }

    @Test
    public void updateTaskPerformerInvalidValue() throws Exception {
        taskPerformerInvalidValue(MockMvcRequestBuilders.put("/tasks/{taskId}", taskRepository.save(defaultTask).getId()),
                jsonProvider.createMap());
    }

    @Test
    public void getTasks() throws Exception {
        Task[] tasks = saveTasks();

        Comment firstComment = commentRepository.save(new Comment(tasks[2].getId(), USER, "Hello (="));
        firstComment = commentRepository.findById(firstComment.getId()).orElseThrow();
        Comment secondComment = commentRepository.save(new Comment(tasks[2].getId(), AUTHOR, "Hi :)"));
        secondComment = commentRepository.findById(secondComment.getId()).orElseThrow();

        Object firstCommentObject = getCommentAsJsonObject(firstComment);
        Object secondCommentObject = getCommentAsJsonObject(secondComment);

        List<Object> commentsArray = jsonProvider.createArray();
        jsonProvider.setArrayIndex(commentsArray, 0, firstCommentObject);
        jsonProvider.setArrayIndex(commentsArray, 1, secondCommentObject);

        Object commentsMap = jsonProvider.createMap();
        jsonProvider.setProperty(commentsMap, "comments", commentsArray);

        Object firstTaskMap = putTaskInMap(getTaskAsJsonObject(tasks[2]));
        jsonProvider.setProperty(firstTaskMap, "comments", commentsArray);
        Object secondTaskMap = putTaskInMap(getTaskAsJsonObject(tasks[3]));
        jsonProvider.setProperty(secondTaskMap, "comments", jsonProvider.createArray());

        Object tasksArray = jsonProvider.createArray();
        jsonProvider.setArrayIndex(tasksArray, 0, firstTaskMap);
        jsonProvider.setArrayIndex(tasksArray, 1, secondTaskMap);
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "tasks", tasksArray);
        jsonProvider.setProperty(map, "total", 5);

        String content = mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?author={a}&performer={p}&status={s}&priority={pr}&offset={o}&limit={l}&comments={c}",
                                AUTHOR, USER, Task.Status.DONE, Task.Priority.HIGH, 1, 2, true)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        helper.assertJsonEqual(jsonProvider.toJson(map), content, true);
    }

    @Test
    public void getTasksNotAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getTasksWithoutFilters() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(getTasksAsMap(new int[]{1, 2, 3})), true));
    }

    @Test
    public void getTasksAuthorNull() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?author={a}", "null")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                        .value("ERROR[400]: Filter(author) can't be null."));
    }

    @Test
    public void getTasksAuthorMe() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?author={a}", "ME")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(getTasksAsMap(new int[]{2, 3})), true));
    }

    @Test
    public void getTasksPerformerNull() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?performer={p}", "null")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(getTasksAsMap(new int[]{1})), true));
    }

    @Test
    public void getTasksPerformerMe() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?performer={p}", "ME")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(getTasksAsMap(new int[]{2})), true));
    }

    @Test
    public void getTasksStatusNull() throws Exception {
        getTasksEnumFilter("status", "null", Task.Status.values());
    }

    @Test
    public void getTasksStatusInvalidValue() throws Exception {
        getTasksEnumFilter("status", "UNKNOWN", Task.Status.values());
    }

    @Test
    public void getTasksPriorityNull() throws Exception {
        getTasksEnumFilter("priority", "null", Task.Priority.values());
    }

    @Test
    public void getTasksPriorityInvalidValue() throws Exception {
        getTasksEnumFilter("priority", "UNKNOWN", Task.Priority.values());
    }

    @Test
    public void getTasksOffsetNegative() throws Exception {
        getTasksLongFilterNegative("offset");
    }

    @Test
    public void getTasksOffsetNotLong() throws Exception {
        getTasksLongFilterNotLong("offset");
    }

    @Test
    public void getTasksOffsetWithoutLimit() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?offset={o}", "10")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                        .value("ERROR[400]: For an offset value > 0 need to provide a limit value > 0."));
    }

    @Test
    public void getTasksLimitNegative() throws Exception {
        getTasksLongFilterNegative("limit");
    }

    @Test
    public void getTasksLimitNotLong() throws Exception {
        getTasksLongFilterNotLong("limit");
    }

    @Test
    public void getTasksSkipMoreThanAvailable() throws Exception {
        long offset = 2;
        long limit = 3;
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?offset={o}&limit={l}", "2", "3")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                        .value(String.format("ERROR[400]: You wanted to skip %d, but after filtering there were only %d items left.",
                                offset * limit, taskRepository.count())));
    }

    @Test
    public void getTasksCommentsFalse() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?comments={c}", "false")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(getTasksAsMap(new int[]{1, 2, 3})), true));
    }

    @Test
    public void getTasksCommentsInvalidValue() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/tasks?comments={c}", "boolean")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                        .value("ERROR[400]: Filter(comments) is not Boolean type."));
    }

    @Test
    public void deleteTask() throws Exception {
        Task task = taskRepository.save(defaultTask);
        mvc.perform(MockMvcRequestBuilders
                        .delete("/tasks/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(putTaskInMap(getTaskAsJsonObject(task)))));
    }

    @Test
    public void deleteTaskNotAuthorized() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .delete("/tasks/{taskId}", taskRepository.save(defaultTask).getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteTaskNotExists() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .delete("/tasks/{taskId}", 0)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                        .value(String.format("ERROR[404]: A task(%d) not exists.", 0)));
    }

    @Test
    public void deleteTaskNotAuthor() throws Exception {
        Task task = taskRepository.save(defaultTask);
        mvc.perform(MockMvcRequestBuilders
                        .delete("/tasks/{taskId}", task.getId())
                        .header("Authorization", "Bearer " + jwtUser))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH)
                        .value(String.format("ERROR[403]: You are not an author of the task(%d).", task.getId())));
    }

    @Test
    public void setTaskStatus() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", "IN_PROCESS");
        Object taskJson = getTaskAsJsonObject(task);
        jsonProvider.setProperty(taskJson, "status", "IN_PROCESS");
        Object expected = putTaskInMap(taskJson);
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/status", task.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(expected)));
    }

    @Test
    public void setTaskStatusNotAuthorized() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", "DONE");
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/status", taskRepository.save(defaultTask).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void setTaskStatusTaskNotExists() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", "DONE");
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/status", 0), jwt,
                map, status().isNotFound(), String.format("ERROR[404]: A task(%d) not exists.", 0));

    }

    @Test
    public void setTaskStatusAsPerformer() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", "DONE");
        Object taskJson = getTaskAsJsonObject(task);
        jsonProvider.setProperty(taskJson, "status", "DONE");
        Object expected = putTaskInMap(taskJson);
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/status", task.getId())
                        .header("Authorization", "Bearer " + jwtUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(expected)));
    }

    @Test
    public void setTaskStatusNotAsAuthorOrPerformer() throws Exception {
        Task task = taskRepository.save(new Task(null, "T", "D", Task.Status.DONE, Task.Priority.HIGH, USER, null));
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", "PENDING");
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/status", task.getId()), jwt,
                map, status().isForbidden(), String.format(
                        "ERROR[403]: You are not an author or a performer of the task(%d).", task.getId()));
    }

    @Test
    public void setTaskStatusNotProvided() throws Exception {
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/status", taskRepository.save(defaultTask).getId()),
                jwt, jsonProvider.createMap(), status().isBadRequest(), String.format(
                        "ERROR[400]: Field(%s) not found.", "status"));
    }

    @Test
    public void setTaskStatusNull() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", null);
        taskFieldNull("status",
                MockMvcRequestBuilders.put("/tasks/{taskId}/status", taskRepository.save(defaultTask).getId()), map);
    }

    @Test
    public void setTaskStatusInvalidValue() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "status", "UNKNOWN");
        taskEnumFieldInvalidValue("status", Task.Status.values(),
                MockMvcRequestBuilders.put("/tasks/{taskId}/status", taskRepository.save(defaultTask).getId()), map);
    }

    @Test
    public void setPerformer() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object performerJson = jsonProvider.createMap();
        jsonProvider.setProperty(performerJson, "performer", AUTHOR);
        Object taskJson = getTaskAsJsonObject(task);
        jsonProvider.setProperty(taskJson, "performer", AUTHOR);
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/performer", task.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(performerJson)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(putTaskInMap(taskJson)), true));
    }

    @Test
    public void setPerformerNotAuthorized() throws Exception {
        Object performerJson = jsonProvider.createMap();
        jsonProvider.setProperty(performerJson, "performer", AUTHOR);
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/performer", taskRepository.save(defaultTask).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(performerJson)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void setTaskPerformerTaskNotExists() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "performer", AUTHOR);
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/performer", 0), jwt,
                map, status().isNotFound(), String.format("ERROR[404]: A task(%d) not exists.", 0));
    }

    @Test
    public void setTaskPerformerNotAuthor() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "performer", AUTHOR);
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/performer", task.getId()),
                jwtUser, map, status().isForbidden(), String.format(
                        "ERROR[403]: You are not an author of the task(%d).", task.getId()));
    }

    @Test
    public void setTaskPerformerNotProvided() throws Exception {
        taskStringFieldNotProvided("performer",
                MockMvcRequestBuilders.put("/tasks/{taskId}/performer", taskRepository.save(defaultTask).getId()),
                jsonProvider.createMap());
    }

    @Test
    public void setTaskPerformerNull() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "performer", null);
        Object taskJson = getTaskAsJsonObject(task);
        jsonProvider.setProperty(taskJson, "performer", null);
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/performer", task.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(jsonProvider.toJson(putTaskInMap(taskJson)), true));
    }

    @Test
    public void setTaskPerformerNotExists() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object map = jsonProvider.createMap();
        String filedName = "performer";
        String email = "unknown@email.me";
        jsonProvider.setProperty(map, filedName, email);
        Object taskJson = getTaskAsJsonObject(task);
        jsonProvider.setProperty(taskJson, "performer", null);
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/performer", task.getId()),
                jwt, map, status().isBadRequest(), String.format(
                        "ERROR[400]: Field(%s) can't be set, because user with specified email(%s) not exists.",
                        filedName, email));
    }

    @Test
    public void addComment() throws Exception {
        Task task = taskRepository.save(defaultTask);
        Object map = jsonProvider.createMap();
        String text = "Hello World!";
        jsonProvider.setProperty(map, "text", text);
        String content = mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/comment", task.getId())
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        Object commentObject = jsonProvider.getMapValue(jsonProvider.parse(content), "comment");
        Assertions.assertEquals(task.getId(), ((Integer) (jsonProvider.getMapValue(commentObject, "taskId"))).longValue());
        Assertions.assertEquals(AUTHOR, jsonProvider.getMapValue(commentObject, "author"));
        Assertions.assertEquals(text, jsonProvider.getMapValue(commentObject, "text"));
    }

    @Test
    public void addCommentNotAuthorized() throws Exception {
        Object map = jsonProvider.createMap();
        String text = "Hello World!";
        jsonProvider.setProperty(map, "text", text);
        mvc.perform(MockMvcRequestBuilders
                        .put("/tasks/{taskId}/comment", taskRepository.save(defaultTask).getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(map)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void addCommentTaskNotExists() throws Exception {
        Object map = jsonProvider.createMap();
        String text = "Hello World!";
        jsonProvider.setProperty(map, "text", text);
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/comment", 0), jwt,
                map, status().isNotFound(), String.format("ERROR[404]: A task(%d) not exists.", 0));
    }

    @Test
    public void addCommentTextNull() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "text", null);
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/comment", taskRepository.save(defaultTask).getId()),
                jwt, map, status().isBadRequest(), "ERROR[400]: Comment text can't be null.");
    }

    @Test
    public void addCommentTextNotProvided() throws Exception {
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/comment", taskRepository.save(defaultTask).getId()),
                jwt, jsonProvider.createMap(), status().isBadRequest(), "ERROR[400]: Field(text) not found.");
    }

    @Test
    public void addCommentTextInvalidValue() throws Exception {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "text", "a".repeat(MAX_COMMENT_LENGTH + 1));
        errorRequest(MockMvcRequestBuilders.put("/tasks/{taskId}/comment", taskRepository.save(defaultTask).getId()),
                jwt, map, status().isBadRequest(), String.format("ERROR[400]: Comment text exceeds max length(%d > %d).",
                        MAX_COMMENT_LENGTH + 1, MAX_COMMENT_LENGTH));
    }
    
    private Object getTaskAsJsonObject() {
        Object taskMap = jsonProvider.createMap();
        jsonProvider.setProperty(taskMap, "title", "Task");
        jsonProvider.setProperty(taskMap, "description", "Description");
        jsonProvider.setProperty(taskMap, "status", "PENDING");
        jsonProvider.setProperty(taskMap, "priority", "LOW");
        jsonProvider.setProperty(taskMap, "performer", "user@mail.ru");
        jsonProvider.setProperty(taskMap, "author", "admin@sb.ru");
        return taskMap;
    }

    private void removeIdFromTask(Object jsonTask) {
        Object task = jsonProvider.getMapValue(jsonTask, "task");
        jsonProvider.removeProperty(task, "id");
    }

    private Object putTaskInMap(Object task) {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "task", task);
        return map;
    }

    private void taskStringFieldNotProvided(String fieldName, MockHttpServletRequestBuilder requestBuilder,
                                            Object body) throws Exception {
        errorRequest(requestBuilder, jwt, body, status().isBadRequest(),
                String.format("ERROR[400]: Field(%s) not found.", fieldName));
    }

    private void taskFieldNull(String fieldName, MockHttpServletRequestBuilder requestBuilder,
                               Object taskMap) throws Exception {
        jsonProvider.setProperty(taskMap, fieldName, null);

        errorRequest(requestBuilder, jwt, taskMap, status().isBadRequest(),
                String.format("ERROR[400]: Field(%s) can not be null.", fieldName));
    }

    private void taskStringFieldInvalidLength(String fieldName, int maxLength, MockHttpServletRequestBuilder requestBuilder,
                                              Object taskMap) throws Exception {
        jsonProvider.setProperty(taskMap, fieldName, "a".repeat(Math.max(0, maxLength + 1)));

        errorRequest(requestBuilder, jwt, taskMap, status().isBadRequest(), String.format("ERROR[400]: Field(%s) exceeds max length(%d > %d).",
                fieldName, maxLength + 1, maxLength));
    }

    private void addTaskEnumFieldNotProvided(String fieldName, String defaultValue) throws Exception {
        Object taskMap = getTaskAsJsonObject();
        jsonProvider.removeProperty(taskMap, fieldName);

        String content = mvc.perform(MockMvcRequestBuilders
                        .post("/tasks")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(taskMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        jsonProvider.setProperty(taskMap, fieldName, defaultValue);
        Object actualTask = jsonProvider.parse(content);
        removeIdFromTask(actualTask);
        helper.assertJsonEqual(jsonProvider.toJson(putTaskInMap(taskMap)), jsonProvider.toJson(actualTask), true);
    }

    private void taskEnumFieldInvalidValue(String fieldName, Object[] validValues,
                                           MockHttpServletRequestBuilder requestBuilder, Object jsonMap) throws Exception {
        jsonProvider.setProperty(jsonMap, fieldName, "UNKNOWN");

        errorRequest(requestBuilder, jwt, jsonMap, status().isBadRequest(), String.format("ERROR[400]: Invalid value for %s, valid values are %s.",
                fieldName, Arrays.toString(validValues)));
    }

    private void taskPerformerNullOrNotProvided(Object taskMap, Object expectedTaskMap, MockHttpServletRequestBuilder requestBuilder,
                                                String requestType) throws Exception {
        String content = mvc.perform(requestBuilder
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(taskMap)))
                .andExpect(
                        switch (requestType) {
                            case "post" -> status().isCreated();
                            case "put" -> status().isOk();
                            default -> status().is(-1);
                        })
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        jsonProvider.setProperty(taskMap, "performer", null);
        Object actualTask = jsonProvider.parse(content);
        removeIdFromTask(actualTask);
        helper.assertJsonEqual(jsonProvider.toJson(putTaskInMap(expectedTaskMap)), jsonProvider.toJson(actualTask), true);
    }

    private void taskPerformerInvalidValue(MockHttpServletRequestBuilder requestBuilder, Object taskMap) throws Exception {
        String email = "unknown@email.me";
        jsonProvider.setProperty(taskMap, "performer", email);

        errorRequest(requestBuilder, jwt, taskMap, status().isBadRequest(), String.format("ERROR[400]: Field(%s) can't be set, because user with specified email(%s) not exists.",
                "performer", email));
    }

    private Task[] saveTasks() {
        Task[] tasks = new Task[9];
        tasks[0] = taskRepository.save(new Task(null, "First", "January", Task.Status.DONE, Task.Priority.HIGH, AUTHOR, USER));
        tasks[1] = taskRepository.save(new Task(null, "Second", "February", Task.Status.DONE, Task.Priority.HIGH, AUTHOR, USER));
        tasks[2] = taskRepository.save(new Task(null, "Third", "March", Task.Status.DONE, Task.Priority.HIGH, AUTHOR, USER));
        tasks[3] = taskRepository.save(new Task(null, "Fourth", "April", Task.Status.DONE, Task.Priority.HIGH, AUTHOR, USER));
        tasks[4] = taskRepository.save(new Task(null, "Fifth", "May", Task.Status.DONE, Task.Priority.HIGH, AUTHOR, USER));
        tasks[5] = taskRepository.save(new Task(null, "Sixth", "June", Task.Status.DONE, Task.Priority.HIGH, USER, null));
        tasks[6] = taskRepository.save(new Task(null, "Seventh", "July", Task.Status.PENDING, Task.Priority.HIGH, AUTHOR, USER));
        tasks[7] = taskRepository.save(new Task(null, "Eighth", "August", Task.Status.DONE, Task.Priority.LOW, AUTHOR, USER));
        tasks[8] = taskRepository.save(new Task(null, "Ninth", "September", Task.Status.DONE, Task.Priority.LOW, AUTHOR, AUTHOR));
        return tasks;
    }

    private Object getCommentAsJsonObject(Comment comment) {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "id", comment.getId());
        jsonProvider.setProperty(map, "taskId", comment.getTaskId());
        jsonProvider.setProperty(map, "author", comment.getAuthor());
        jsonProvider.setProperty(map, "text", comment.getText());
        jsonProvider.setProperty(map, "date", comment.getDate());
        return map;
    }

    private Object getTaskAsJsonObject(Task task) {
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "id", task.getId());
        jsonProvider.setProperty(map, "title", task.getTitle());
        jsonProvider.setProperty(map, "description", task.getDescription());
        jsonProvider.setProperty(map, "status", task.getStatus());
        jsonProvider.setProperty(map, "priority", task.getPriority());
        jsonProvider.setProperty(map, "author", task.getAuthor());
        jsonProvider.setProperty(map, "performer", task.getPerformer());
        return map;
    }

    private Object getTasksAsMap(int[] ids) {
        Task[] tasks = new Task[ids.length];
        for (int i = 0; i < tasks.length; i++) {
            tasks[i] = taskRepository.findById((long) ids[i]).orElseThrow();
        }

        Object tasksArray = jsonProvider.createArray();
        for (int i = 0; i < tasks.length; i++) {
            jsonProvider.setArrayIndex(tasksArray, i, getTaskAsJsonObject(tasks[i]));
        }
        Object map = jsonProvider.createMap();
        jsonProvider.setProperty(map, "tasks", tasksArray);
        jsonProvider.setProperty(map, "total", tasks.length);
        return map;
    }

    private void getTasksEnumFilter(String filterName, String filterValue, Object[] expectedValues) throws Exception {
        errorFilterRequest(MockMvcRequestBuilders.get(String.format("/tasks?%s={%s}", filterName, filterName.charAt(0)), filterValue),
                String.format("ERROR[400]: Filter(%s) is not one of the expected value %s.", filterName, Arrays.toString(expectedValues)));
    }

    private void getTasksLongFilterNegative(String filterName) throws Exception {
        errorFilterRequest(MockMvcRequestBuilders.get(String.format("/tasks?%s={%s}", filterName, filterName.charAt(0)), -1),
                String.format("ERROR[400]: Filter(%s) can't have a negative value.", filterName));
    }

    private void getTasksLongFilterNotLong(String filterName) throws Exception {
        errorFilterRequest(MockMvcRequestBuilders.get(String.format("/tasks?%s={%s}", filterName, filterName.charAt(0)), "null"),
                String.format("ERROR[400]: Filter(%s) is not Long type.", filterName));
    }

    private void errorRequest(MockHttpServletRequestBuilder requestBuilder,
                              String jwt, Object body,
                              ResultMatcher status, String errorMessage) throws Exception {
        mvc.perform(requestBuilder
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonProvider.toJson(body)))
                .andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(errorMessage));
    }

    private void errorFilterRequest(MockHttpServletRequestBuilder requestBuilder, String errorMessage) throws Exception {
        mvc.perform(requestBuilder
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(ERROR_MESSAGE_PATH).value(errorMessage));
    }
}
