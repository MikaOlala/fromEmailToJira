package org.example.models;

public class JiraTask {
    private String name;
    private String summary;

    public JiraTask(String name, String summary) {
        this.name = name;
        this.summary = summary;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
