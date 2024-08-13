package ru.sb.config.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import ru.sb.model.Comment;
import ru.sb.model.Task;


@OpenAPIDefinition(
        info = @Info(
                title = "Task Management System",
                version = "0.0.1",
                description = "Simple Task Management System."
        ),
        tags = {
                @Tag(name = "users", description = "Users management."),
                @Tag(name = "tasks", description = "Tasks management.")
        },
        servers = {
                @Server(
                        description = "Local test server.",
                        url = "http://localhost:8080")
        }
)
@SecurityScheme(name = "JWT", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenAPIConfig {

    public static class UserSchema {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String email;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public String password;
    }

    public static class ErrorSchema {
        @Schema(name = "error message")
        public String errorMessage;
    }

    public static class TaskAddSchema {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 50)
        public String title;
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 300)
        public String description;
        @Schema(description = "Default value PENDING.")
        public Task.Status status;
        @Schema(description = "Default value LOW.")
        public Task.Priority priority;
        @Schema(description = "Default value null.", nullable = true, maxLength = 30)
        public String performer;
    }

    public static class TokenSchema{
        public String token;
    }

    public static class TaskUpdateSchema {
        @Schema(maxLength = 50)
        public String title;
        @Schema(maxLength = 300)
        public String description;
        public Task.Status status;
        public Task.Priority priority;
        @Schema(maxLength = 30, nullable = true)
        public String performer;
    }

    public static class TaskOutputSchema {
        public Long id;
        public String title;
        public String description;
        public Task.Status status;
        public Task.Priority priority;
        public String author;
        public String performer;
    }

    public static class TasksSchema {
        public Task[] tasks;
        public Integer total;
    }

    public static class TasksCommentsSchema {
        public TaskCommentsSchema[] tasks;
        public Integer total;
    }

    public static class TaskCommentsSchema {
        public Task task;
        public Comment comments;
    }

    public static class CommentSchema {
        public Long id;
        public Long taskId;
        public String author;
        public String text;
        @Schema(description = "Date format ISO 8601.")
        public String date;
    }

    public static class TaskUpdateStatusSchema {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        public Task.Status status;
    }

    public static class TaskUpdatePerformerSchema {
        @Schema(maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED, nullable = true)
        public String performer;
    }

    public static class CommentAddSchema {
        @Schema(maxLength = 300, requiredMode = Schema.RequiredMode.REQUIRED)
        public String text;
    }
}
