package ru.sb.service;

import ru.sb.model.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> findAllByTaskId(Long taskId);
}
