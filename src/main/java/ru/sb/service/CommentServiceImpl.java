package ru.sb.service;

import org.springframework.stereotype.Service;
import ru.sb.model.Comment;
import ru.sb.model.CommentRepository;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private CommentRepository commentRepository;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> findAllByTaskId(Long taskId) {
        return commentRepository.findAllByTaskId(taskId);
    }
}
