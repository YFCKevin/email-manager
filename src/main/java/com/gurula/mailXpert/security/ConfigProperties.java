package com.gurula.mailXpert.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class
ConfigProperties {
    @Value("${spring.security.oauth2.client.registration.google1.client-id}")
    private String clientId1;
    @Value("${spring.security.oauth2.client.registration.google1.client-secret}")
    private String clientSecret1;
    @Value("${spring.security.oauth2.client.registration.google1.redirect-uri}")
    private String redirectUri1;
    @Value("${spring.security.oauth2.client.registration.google2.client-id}")
    private String clientId2;
    @Value("${spring.security.oauth2.client.registration.google2.client-secret}")
    private String clientSecret2;
    @Value("${spring.security.oauth2.client.registration.google2.redirect-uri}")
    private String redirectUri2;
    @Value("${spring.data.mongodb.uri}")
    public String mongodbUri;

    public String getClientId1() {
        return clientId1;
    }

    public String getClientSecret1() {
        return clientSecret1;
    }

    public String getRedirectUri1() {
        return redirectUri1;
    }

    public String getClientId2() {
        return clientId2;
    }

    public String getClientSecret2() {
        return clientSecret2;
    }

    public String getRedirectUri2() {
        return redirectUri2;
    }

    public String getMongodbUri() {
        return mongodbUri;
    }

    public void setMongodbUri(String mongodbUri) {
        this.mongodbUri = mongodbUri;
    }

    public void setClientId1(String clientId1) {
        this.clientId1 = clientId1;
    }

    public void setClientSecret1(String clientSecret1) {
        this.clientSecret1 = clientSecret1;
    }

    public void setRedirectUri1(String redirectUri1) {
        this.redirectUri1 = redirectUri1;
    }

    public void setClientId2(String clientId2) {
        this.clientId2 = clientId2;
    }

    public void setClientSecret2(String clientSecret2) {
        this.clientSecret2 = clientSecret2;
    }

    public void setRedirectUri2(String redirectUri2) {
        this.redirectUri2 = redirectUri2;
    }
}
