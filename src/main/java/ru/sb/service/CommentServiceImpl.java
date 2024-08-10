package ru.sb.service;

import org.springframework.stereotype.Service;
import ru.sb.model.Comment;
import ru.sb.model.CommentRepository;

import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {
    private CommentRepository commentRepository;
    private static final int MAX_COMMENT_LENGTH = 300;

    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<Comment> findAllByTaskId(Long taskId) {
        return commentRepository.findAllByTaskId(taskId);
    }

    @Override
    public Map<String, Object> addComment(Long taskId, String author, String text) {
        if (text == null) {
            return Map.of("message", "ERROR[400]: Comment text can't be null.");
        } else if (text.length() > MAX_COMMENT_LENGTH) {
            return Map.of("message", String.format("ERROR[400]: Comment text exceeds max length(%d > %d).",
                    text.length(), MAX_COMMENT_LENGTH));
        }
        return Map.of("comment", commentRepository.save(new Comment(taskId, author, text)));
    }
}
