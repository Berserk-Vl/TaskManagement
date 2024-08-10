package ru.sb.service;

import ru.sb.model.Comment;

import java.util.List;
import java.util.Map;

public interface CommentService {
    List<Comment> findAllByTaskId(Long taskId);

    Map<String, Object> addComment(Long taskId, String author, String text);
}
