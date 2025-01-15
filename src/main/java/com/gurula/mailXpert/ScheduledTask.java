package com.gurula.mailXpert;

import com.google.api.services.gmail.model.Message;
import com.gurula.mailXpert.gmail.GmailService;
import com.gurula.mailXpert.oauth2.OAuthToken;
import com.gurula.mailXpert.oauth2.OAuthTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ScheduledTask {
    protected Logger logger = LoggerFactory.getLogger(ScheduledTask.class);
    @Autowired
    private GmailService gmailService;
    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;

    @Scheduled(cron = "0 0 9 * * ?")
    @GetMapping("/manageEmails")
    public void manageEmails () throws GeneralSecurityException, IOException {

        String todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt";

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        final Map<String, String> emailAccessTokenMap = oAuthTokenRepository.findAll().stream().collect(Collectors.toMap(OAuthToken::getUserId, OAuthToken::getAccessToken));

        for (Map.Entry<String, String> entry : emailAccessTokenMap.entrySet()) {

            String email = entry.getKey();
            System.out.println("email = " + email);
            String accessToken = entry.getValue();
            System.out.println("accessToken = " + accessToken);

            // 取得該帳號的未讀信件
            List<Message> newUnreadMessages = gmailService.getNewUnreadMessages(email, accessToken, today, tomorrow);
            final List<String> messageIds = newUnreadMessages.stream().map(Message::getId).toList();

            if (messageIds.size() > 0) {
                final List<Message> messageDetails = gmailService.getMessageDetails(email, accessToken, messageIds);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(new File("data"), fileName), true))) {
                    for (Message message : messageDetails) {

                        long internalDateMillis = message.getInternalDate();
                        String receivedDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(internalDateMillis));

                        // 內容格式
                        StringBuilder content = new StringBuilder();
                        content.append("今日日期: ").append(todayDate).append("\n");
                        content.append("收件者: ").append(email).append("\n");
                        content.append("收件日期時間: ").append(receivedDateTime).append("\n");
                        content.append("內容: ").append(message.getSnippet()).append("\n");
                        content.append("====================================\n");

                        writer.write(content.toString());

                        logger.info("Account: {}", email);
                        logger.info("History ID: {}", message.getHistoryId());
                        logger.info("ID: {}", message.getId());
                        logger.info("Internal Date: {}", message.getInternalDate());
                        logger.info("Label IDs: {}", message.getLabelIds());
                        logger.info("Payload: {}", message.getPayload());
                        logger.info("Raw: {}", message.getRaw());
                        logger.info("Size Estimate: {}", message.getSizeEstimate());
                        logger.info("Snippet: {}", message.getSnippet());
                        logger.info("Thread ID: {}", message.getThreadId());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("Account: {} 今日無未讀信件", email);
            }
        }

    }
}
