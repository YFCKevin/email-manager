package com.gurula.mailXpert.oauth2;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "oAuth_token")
public class OAuthToken {
    @Id
    private String id;
    private String userId;  // email
    private String oauth2ClientName;
    private String accessToken;
    private String refreshToken;
    private Long tokenExpiry;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOauth2ClientName() {
        return oauth2ClientName;
    }

    public void setOauth2ClientName(String oauth2ClientName) {
        this.oauth2ClientName = oauth2ClientName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(Long tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
}
