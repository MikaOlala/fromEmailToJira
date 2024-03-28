package org.example;

import java.io.IOException;
import java.lang.System;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.models.Mail;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.example.Main.getCalendarFromString;

public class Graph {

    private static Graph instance;
    private static final String clientId = "client-id";
    private static final String clientSecret = "client-secret";

    private static  OAuth20Service service;
    private static String code = "";

    public Graph() {
    }
    public static Graph getInstance() {
        if (instance==null)
            instance = new Graph();

        return instance;
    }

    public static void main(String[] args) {}

    public void start() {
        String secretState = "secret" + new Random().nextInt(999_999);

        service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .defaultScope("User.Read Mail.Read.Shared offline_access")
                .callback("https://www.google.ru/")
                .userAgent("ScribeJava")
                .build(GraphApi.getInstance());

        System.out.println("Fetching the Authorization URL...");
        final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

        final String authorizationUrl = service.getAuthorizationUrl(secretState);
        System.out.println("Got the Authorization URL!");
        System.out.println("Now go and authorize ScribeJava here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization url here");
        System.out.print(">>");
        code = in.nextLine();
        System.out.println();

        code = code.substring(code.indexOf("=")+1, code.indexOf("&"));
        System.out.println(code);
    }

    public OAuth2AccessToken getToken() {
        System.out.println("Trading the Authorization Code for an Access Token...");
        OAuth2AccessToken accessToken = null;
        try {
            accessToken = service.getAccessToken(code);
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Got the Access Token!");

        return accessToken;
    }

    public OAuth2AccessToken refreshToken(OAuth2AccessToken accessToken) {
        System.out.println("Refreshing the Access Token...");
        try {
            accessToken = service.refreshAccessToken(accessToken.getRefreshToken());
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("Refreshed the Access Token!");
        return accessToken;
    }

    public JsonArray getMail(OAuth2AccessToken accessToken) {
        if (accessToken==null) {
            System.out.println("accessToken IS NULL IN Graph.getMail");
            return null;
        }

        String response = Main.makeRequest("https://graph.microsoft.com/v1.0/users/mail@mail.kz/messages", false, null);

        return Main.extractValue(
                response, "value"
        );
    }



    public ArrayList<Mail> chooseNew(JsonArray array) {
        if (array==null) {
            System.out.println("ARRAY IS NULL IN Graph.chooseNew");
            return null;
        }
        ArrayList<Mail> list = new ArrayList<>();

        for (JsonElement arr : array) {
            JsonObject json = arr.getAsJsonObject();
            String date = json.get("receivedDateTime").getAsString();

            if (!isDateWeNeed(date))
                continue;

            list.add(new Mail(
                    json.get("id").getAsString(),
                    json.get("subject").getAsString(),
                    date,
                    json.get("body").getAsJsonObject().get("content").getAsString(),
                    json.get("importance").getAsString()
            ));
        }

        System.out.println("From " + array.size() + " mails there is " + list.size() + " new ");

        return list;
    }

    private static boolean isDateWeNeed(String dateStr) {
        Calendar calendar = getCalendarFromString(dateStr);
        if (calendar==null)
            return false;

        LocalDate thatDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE));

        LocalDate todaysDate = LocalDate.now();
        long daysDiff = ChronoUnit.DAYS.between(thatDate, todaysDate);

        return daysDiff <= Main.daysRange;
    }
}
