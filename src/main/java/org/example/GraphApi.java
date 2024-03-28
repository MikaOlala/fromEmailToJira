package org.example;

import com.github.scribejava.core.builder.api.DefaultApi20;

public class GraphApi extends DefaultApi20 {

    private static GraphApi instance;
    private static final String tenantId = "tenantId";

    public GraphApi() {
    }
    public static synchronized GraphApi getInstance() {
        if (instance == null) {
            instance = new GraphApi();
        }
        return instance;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize";
    }
}
