package com.gurula.mailXpert.gmail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.gurula.mailXpert.oauth2.OAuthTokenRepository;
import com.gurula.mailXpert.security.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GmailService {
    private static final String APPLICATION_NAME = "MailXpert";
    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;
    @Autowired
    private ConfigProperties configProperties;

    public Gmail getGmailService(String email, String accessToken, String refreshToken) throws IOException, GeneralSecurityException {

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Access token is missing");
        } else if (email == null || email.isEmpty()) {
            throw new IOException("email is missing");
        }

        // 3. 使用 access token 建立 Gmail service
        JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // 使用存儲的 access token 建立 Google Credential
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jacksonFactory)
                .setClientSecrets(configProperties.getClientId(), configProperties.getClientSecret())
                .build()
                .setAccessToken(accessToken)
                .createScoped(Arrays.asList(
                        GmailScopes.GMAIL_READONLY,   // 讀取郵件
                        GmailScopes.GMAIL_LABELS,     // 操作標籤
                        GmailScopes.GMAIL_MODIFY))    // 修改郵件狀態（例如設置為已讀）
                .setRefreshToken(refreshToken);

        // 如果 access token 已過期，嘗試使用 refresh token 更新
        if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() <= 60) {
            System.out.println("Access token is expired or about to expire, refreshing...");
            refreshAccessToken(credential, configProperties.getClientId(), configProperties.getClientSecret(), email);
        }

        return new Gmail.Builder(httpTransport, jacksonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    private void refreshAccessToken(GoogleCredential credential, String clientId, String clientSecret, String email) throws IOException {
        // 發送 POST 請求到 Google 的 Token Endpoint
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpRequestFactory requestFactory = credential.getTransport().createRequestFactory();
        GenericUrl url = new GenericUrl(tokenUrl);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("client_id", clientId);
        parameters.put("client_secret", clientSecret);
        parameters.put("refresh_token", credential.getRefreshToken());
        parameters.put("grant_type", "refresh_token");

        HttpContent content = new UrlEncodedContent(parameters);
        HttpRequest request = requestFactory.buildPostRequest(url, content);

        HttpResponse response = request.execute();
        Map<String, Object> responseData = new ObjectMapper().readValue(response.getContent(), Map.class);

        // 更新 access token 和過期時間
        String newAccessToken = (String) responseData.get("access_token");
        Integer expiresIn = (Integer) responseData.get("expires_in");
        oAuthTokenRepository.findByUserId(email).ifPresent(oAuthToken -> {
            oAuthToken.setAccessToken(newAccessToken);
            oAuthToken.setTokenExpiry(Long.valueOf(expiresIn));
        });

        credential.setAccessToken(newAccessToken);
        if (expiresIn != null) {
            credential.setExpiresInSeconds((long) expiresIn);
        }

        System.out.println("Access token refreshed successfully.");
    }


    public List<Message> getNewUnreadMessages(String email, String accessToken, String refreshToken, String today, String yesterday) throws IOException, GeneralSecurityException {
        final Gmail service = getGmailService(email, accessToken, refreshToken);
        List<Message> messages = new java.util.ArrayList<>();
        String query = "is:unread after:" + yesterday + " before:" + today;
        String pageToken = null;
        do {
            ListMessagesResponse response = service.users().messages()
                    .list("me")
                    .setQ(query)
                    .setPageToken(pageToken)
                    .execute();
            if (response != null && response.getMessages() != null){
                messages.addAll(response.getMessages());
                pageToken = response.getNextPageToken();
            } else {
                break;
            }

        } while (pageToken != null);

        return messages;
    }

    public List<Message> getMessageDetails(String email, String accessToken, String refreshToken, List<String> messageIds) throws IOException, GeneralSecurityException {
        final Gmail service = getGmailService(email, accessToken, refreshToken);
        List<Message> messages = new ArrayList<>();

        for (String messageId : messageIds) {
            Message message = service.users().messages().get("me", messageId)
                    .setFormat("metadata")
                    .execute();
            messages.add(message);
        }

        return messages;
    }
}
