package com.gurula.mailXpert.gmail;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GmailService {
    private static final String APPLICATION_NAME = "MailXpert";
    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;
    @Autowired
    private ConfigProperties configProperties;

    public Gmail getGmailService(String email, String accessToken) throws IOException, GeneralSecurityException {

        if (accessToken == null || accessToken.isEmpty()) {
            throw new IOException("Access token is missing");
        } else if (email == null || email.isEmpty()) {
            throw new IOException("email is missing");
        }

        // 3. 使用 access token 建立 Gmail service
        JacksonFactory jacksonFactory = JacksonFactory.getDefaultInstance();

        String clientId;
        String clientSecret;

        switch (email) {
            case "yifanchen0914@gmail.com" -> {
                System.out.println("123");
                clientId = configProperties.getClientId1();
                System.out.println("clientId = " + clientId);
                clientSecret = configProperties.getClientSecret1();
                System.out.println("clientSecret = " + clientSecret);
            }
            case "pigmonkey0921@gmail.com" -> {
                System.out.println("456");
                clientId = configProperties.getClientId2();
                System.out.println("clientId = " + clientId);
                clientSecret = configProperties.getClientSecret2();
                System.out.println("clientSecret = " + clientSecret);
            }
            default -> throw new IOException("Unsupported email: " + email);
        }

        // 使用存儲的 access token 建立 Google Credential
        Credential authorize = new GoogleCredential.Builder().setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(jacksonFactory)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setAccessToken(accessToken)
                .createScoped(Arrays.asList(
                        GmailScopes.GMAIL_READONLY,   // 讀取郵件
                        GmailScopes.GMAIL_LABELS,     // 操作標籤
                        GmailScopes.GMAIL_MODIFY))    // 修改郵件狀態（例如設置為已讀）
                .setAccessToken(accessToken);

        // Create Gmail service
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        return new Gmail.Builder(HTTP_TRANSPORT, jacksonFactory, authorize)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Message> getNewUnreadMessages(String email, String accessToken, String today, String tomorrow) throws IOException, GeneralSecurityException {
        final Gmail service = getGmailService(email, accessToken);
        List<Message> messages = new java.util.ArrayList<>();
        String query = "is:unread after:" + today + " before:" + tomorrow;
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

    public List<Message> getMessageDetails(String email, String accessToken, List<String> messageIds) throws IOException, GeneralSecurityException {
        final Gmail service = getGmailService(email, accessToken);
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
