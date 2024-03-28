package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.models.JiraTask;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

public class Jira {
//    private static Jira instance;
    private static final String URI = "https://jira.atlassian.com/rest/api/latest/";
    private static final String searchParameters = "search/?jql=project=TEST AND updatedDate>='%s' AND updatedDate<='%s'";
    private static final String login = "mail@mail.kz";
    private static final String token = "token";
    private static final String assigneeId = "assigneeId";
    private static final String projectName = "TEST";
    private static final String issueTypeName = "Task";
    public static String ISSUE = "issue";
    public static String TASK = "issue/Task-1000";


//    public Jira() {
//    }
//
//    public static Jira getInstance() {
//        if (instance==null)
//            instance = new Jira();
//
//        return instance;
//    }

    public static String getEncodedAuth() {
        String auth = login + ":" + token;
        return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes());
    }

    public static String createJson(String summary, String description) {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();

        JsonObject project = new JsonObject();
        project.addProperty("key", projectName);

        JsonObject assignee = new JsonObject();
        assignee.addProperty("accountId", assigneeId);

        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", issueTypeName);

        json.add("project", project);
        json.addProperty("summary", summary);
        json.addProperty("description", description);
        json.add("assignee", assignee);
        json.add("issuetype", issueType);

        JsonObject fields = new JsonObject();
        fields.add("fields", json);

        return gson.toJson(fields);
    }

    public static ArrayList<String> getTaskListFromJira() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(Main.daysRange);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String parameters = String.format(searchParameters, startDate.format(formatter), today.format(formatter));

        parameters = parameters.replace(">", "%3E");
        parameters = parameters.replace("<", "%3C");
        parameters = parameters.replace(" ", "+");

        String response = Main.makeRequest(URI + parameters, true, null);

        JsonArray array = Main.extractValue(response, "issues");
        if (array==null) {
            System.out.println("array IS NULL IN Jira.getTaskListFromJira");
            return null;
        }

        ArrayList<String> list = new ArrayList<>();
        for (JsonElement arr : array) {
            JsonObject json = arr.getAsJsonObject();
            JsonObject fields = json.get("fields").getAsJsonObject();

            if (fields==null) {
                System.out.println("Element in array is not found Jira.getTaskListFromJira");
                continue;
            }

            list.add(fields.get("summary").getAsString());
        }

        System.out.println();
        System.out.println("Jira Tasks exists: " + list.size());
        System.out.println(list);

        return list;
    }

    public static String getURI() {
        return URI;
    }

    public static String getAssigneeId() {
        return assigneeId;
    }

    public static String getISSUE() {
        return ISSUE;
    }

    public static String getTASK() {
        return TASK;
    }
}
