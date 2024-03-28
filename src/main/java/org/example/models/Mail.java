package org.example.models;

import java.nio.charset.StandardCharsets;

public class Mail {
    private String id;
    private String title;
    private String date;
    private String content;
    private String importance;

    public Mail(String id, String title, String date, String content, String importance) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.content = content;
        this.importance = importance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "id=" + id + '\n' +
                ", title=" + title + '\n' +
                ", date=" + date + '\n' +
                ", content=" + content + '\n' +
                ", importance=" + importance +
                '}';
    }
}
