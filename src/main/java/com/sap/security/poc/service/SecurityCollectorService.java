package com.sap.security.poc.service;

import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sap.security.poc.config.SecurityCollectorConfiguration;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class SecurityCollectorService {

    private String token;
    private Object lockToken = new Object();
    private final Gson gson = new Gson();

    private final OAuth2Service oauth2;
    private final SecurityCollectorConfiguration securityCollectorConfiguration;


    @Autowired
    public SecurityCollectorService(OAuth2Service oauth2, SecurityCollectorConfiguration securityCollectorConfiguration) {
        this.oauth2 = oauth2;
        this.securityCollectorConfiguration = securityCollectorConfiguration;
    }


    public  Object[] getRawData() throws OAuthProblemException, OAuthSystemException, IOException, InterruptedException {
        if (token == null || token == "") {
            synchronized (lockToken) {
                if (token == null || token == "") {
                    token = oauth2.getAccessToken();
                }
            }
        }


        HttpResponse<?> response = request(securityCollectorConfiguration.getUrl() + "/data", token);
        if (response.statusCode() == 403 || response.statusCode() == 401) {
            synchronized (lockToken) {
                token = oauth2.getAccessToken();
            }
            response = request(securityCollectorConfiguration.getUrl() + "/data", token);
        }

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body().toString(), Object[].class);
        }
        return new Object[]{};
    }

    public Object getRecommendations(String type) throws OAuthProblemException, OAuthSystemException, IOException, InterruptedException {
        if (token == null || token == "") {
            synchronized (lockToken) {
                if (token == null || token == "") {
                    token = oauth2.getAccessToken();
                }
            }
        }


        HttpResponse<?> response = request(securityCollectorConfiguration.getUrl() + "/recommendations?type=" + type, token);
        if (response.statusCode() == 403 || response.statusCode() == 401) {
            synchronized (lockToken) {
                token = oauth2.getAccessToken();
            }
            response = request(securityCollectorConfiguration.getUrl() + "/recommendations?type=" + type, token);
        }

        if (response.statusCode() == 200) {
            return gson.fromJson(response.body().toString(), Object.class);
        }
        return new Object();
    }

    private HttpResponse<?> request(String url, String token) throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .headers("Content-Type", "application/json", "Authorization", "Bearer "+ token)
                .GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
