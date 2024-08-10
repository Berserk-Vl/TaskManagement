package ru.sb.model;

import jakarta.persistence.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "task_id")
    private Long taskId;
    private String author;
    private String text;
    @Column(name = "timestamp")
    private String date;

    public Comment() {
    }

    public Comment(Long id, Long taskId, String author, String text, String date) {
        this.id = id;
        this.taskId = taskId;
        this.author = author;
        this.text = text;
        this.date = date;
    }

    public Comment(Long taskId, String author, String text) {
        this(null, taskId, author, text, ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
