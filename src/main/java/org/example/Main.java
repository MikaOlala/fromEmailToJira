package org.example;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.text.StringEscapeUtils;
import org.example.models.JiraTask;
import org.example.models.Mail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.Jira.createJson;
import static org.example.Jira.getEncodedAuth;

public class Main {

    private static ArrayList<Mail> list = new ArrayList<>();
    private static ArrayList<String> jiraTasks = new ArrayList<>();
    private static OAuth2AccessToken token;
    public static int daysRange = 10;

    public static void main(String[] args) {
        Graph.getInstance().start();
        token = Graph.getInstance().getToken();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(Main::doMainWork, 0, 10, TimeUnit.MINUTES); // Выполнять метод каждые 10 минут
    }

    private static void doMainWork() {
        System.out.println();
        System.out.println("--- START NEW ITERATION ---");
        System.out.println("token expires in " + token.getExpiresIn());

        JsonArray array = Graph.getInstance().getMail(token);
        list = Graph.getInstance().chooseNew(array);

        if (!list.isEmpty()) {
            cleanMailText();

            jiraTasks = Jira.getTaskListFromJira();
            createNewTasks();
        }

        System.out.println("--- END OF ITERATION ---");
    }

    private static void createNewTasks() {
        for (Mail m : list) {
            boolean taskExists = false;
            for (String title : jiraTasks) {
                if (m.getTitle().equals(title)) {
                    taskExists = true;
                    break;
                }
            }

            if (!taskExists)
                makeRequest(Jira.getURI() + Jira.getISSUE(), true, createJson(m.getTitle(), m.getContent()));
        }
    }

    private static void cleanMailText() {
        for (Mail m : list) {

            String requestNumber = m.getTitle().substring(  // вырезаю номер
                    0,
                    m.getTitle().indexOf(" "));

            Calendar calendar = getCalendarFromString(m.getDate());

            String newTitle = requestNumber + " Request from " +
                    calendar.get(Calendar.DATE) + " " +
                    getNameFromDate(calendar.get(Calendar.MONTH), true) + ", " +
                    getNameFromDate(calendar.get(Calendar.DAY_OF_WEEK), false);

            m.setTitle(newTitle);

            String newContent = StringEscapeUtils.unescapeHtml4(m.getContent())
                    .replace("<br>", "\n")
                    .replaceAll("\\<.*?\\>", "");

            newContent = "Mail received: " + m.getDate() + "\n\n" + newContent;

            m.setContent(newContent);
        }

        System.out.println("Finished cleaning");
    }

    public static String makeRequest(String uri, boolean jiraRequest, String body) {
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(formRequestAzure(uri, jiraRequest, body), HttpResponse.BodyHandlers.ofString());
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("REQUEST EXECUTION FAILED");
            return null;
        }

        if (response==null)
            return "";

        int code = response.statusCode();
        System.out.println("Status Code: " + code);

        if (code==401 && !jiraRequest) {
            token = Graph.getInstance().refreshToken(token);
        }

        return response.body();
    }

    private static HttpRequest formRequestAzure(String uri, boolean jiraRequest, String body) {
        System.out.println("Request with uri:  " + uri);
        HttpRequest request = null;

        try {
            if (jiraRequest && body != null)
                request = HttpRequest.newBuilder()
                        .uri(new URI(uri))
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .header("Authorization", getEncodedAuth())
                        .header("Content-Type", "application/json")
                        .build();

            else if (jiraRequest)
                request = HttpRequest.newBuilder()
                        .uri(new URI(uri))
                        .GET()
                        .header("Authorization", getEncodedAuth())
                        .build();
            else
                request = HttpRequest.newBuilder()
                        .uri(new URI(uri))
                        .GET()
                        .header("Authorization", "Bearer " + token.getAccessToken())
                        .build();

        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.out.println("REQUEST SYNTAX FAILED");
            return null;
        }

        return request;
    }

    public static JsonArray extractValue(String body, String key) {
        if (body==null) {
            System.out.println("body IS NULL IN Main.extractValue");
            return null;
        }

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(body, JsonObject.class);
        return json.getAsJsonArray(key);
    }

    public static Calendar getCalendarFromString(String dateStr) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = null;

        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date==null)
            return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static String getNameFromDate(int number, boolean month) {
        DateFormatSymbols dfs = new DateFormatSymbols();
        String nameResult = "";

        if (month) {
            String[] months = dfs.getMonths();
            nameResult = months[number];
        }
        else {
            // 0 - ничто, 1 - воскр, 2 - понедельник
            String[] weekdays = dfs.getWeekdays();
            nameResult = weekdays[number];
        }

        return nameResult;
    }
}