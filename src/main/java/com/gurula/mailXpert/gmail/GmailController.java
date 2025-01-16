package com.gurula.mailXpert.gmail;

import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.gurula.mailXpert.exception.ResultStatus;
import com.gurula.mailXpert.oauth2.OAuthToken;
import com.gurula.mailXpert.oauth2.OAuthTokenRepository;
import com.gurula.mailXpert.openai.OpenAiService;
import com.gurula.mailXpert.utils.MailUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GmailController {
    protected Logger logger = LoggerFactory.getLogger(GmailController.class);
    @Autowired
    private GmailService gmailService;
    @Autowired
    private OAuthTokenRepository oAuthTokenRepository;
    @Autowired
    private OpenAiService openAiService;

    @Scheduled(cron = "0 0 9 * * ?")
    @GetMapping("/manageEmails")
    public void manageEmails () throws GeneralSecurityException, IOException {

        String todayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".txt";

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        List<String> totalMessageIds = new ArrayList<>();

        final Map<String, Map<String, String>> emailTokenMap = oAuthTokenRepository.findAll().stream()
                .collect(Collectors.toMap(
                        OAuthToken::getUserId,
                        token -> Map.of(
                                "accessToken", token.getAccessToken(),
                                "refreshToken", token.getRefreshToken()
                        )
                ));
        for (Map.Entry<String, Map<String, String>> entry : emailTokenMap.entrySet()) {

            String email = entry.getKey();
            System.out.println("email = " + email);

            Map<String, String> tokens = entry.getValue();
            String accessToken = tokens.get("accessToken");
            String refreshToken = tokens.get("refreshToken");

            System.out.println("accessToken = " + accessToken);
            System.out.println("refreshToken = " + refreshToken);


            // 取得該帳號的未讀信件
            List<Message> newUnreadMessages = gmailService.getNewUnreadMessages(email, accessToken, refreshToken, today, yesterday);
            final List<String> messageIds = newUnreadMessages.stream().map(Message::getId).toList();

            if (messageIds.size() > 0) {
                final List<Message> messageDetails = gmailService.getMessageDetails(email, accessToken, refreshToken, messageIds);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(new File("data"), fileName), true))) {
                    for (Message message : messageDetails) {

                        long internalDateMillis = message.getInternalDate();
                        String receivedDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(internalDateMillis));

                        String sender = "";
                        List<MessagePartHeader> headers = message.getPayload().getHeaders();
                        for (MessagePartHeader header : headers) {
                            if ("From".equalsIgnoreCase(header.getName())) {
                                sender = header.getValue();
                                break;
                            }
                        }

                        // 內容格式
                        StringBuilder content = new StringBuilder();
                        content.append("今日日期: ").append(todayDate).append("\n");
                        content.append("收件者: ").append(email).append("\n");
                        content.append("寄件者: ").append(sender).append("\n");
                        content.append("收件日期時間: ").append(receivedDateTime).append("\n");
                        content.append("內容: ").append(message.getSnippet()).append("\n");
                        content.append("\n");

                        writer.write(content.toString());

                        System.out.println("Account: " + email);
                        System.out.println("History ID: " + message.getHistoryId());
                        System.out.println("ID: " + message.getId());
                        System.out.println("Internal Date: " + message.getInternalDate());
                        System.out.println("Label IDs: " + message.getLabelIds());
                        System.out.println("Payload: " + message.getPayload());
                        System.out.println("Raw: " + message.getRaw());
                        System.out.println("Size Estimate: " + message.getSizeEstimate());
                        System.out.println("Snippet: " + message.getSnippet());
                        System.out.println("Thread ID: " + message.getThreadId());
                    }
                    totalMessageIds.addAll(messageIds);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                logger.info("Account: {} 今日無未讀信件", email);
            }
        }
        if (totalMessageIds.size() > 0) {
            final ResultStatus<String> result = openAiService.genUnreadMsgSummary();
            if ("C000".equals(result.getCode())) {
                final String summary = result.getData();
                MailUtils.sendMail("yifanchen0914@gmail.com", "昨日未讀信件彙整", summary);
            }
        }
    }
}

