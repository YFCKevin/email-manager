package com.gurula.mailXpert.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurula.mailXpert.exception.ResultStatus;
import com.gurula.mailXpert.openai.dto.ChatCompletionResponse;
import com.gurula.mailXpert.security.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAiService {
    protected Logger logger = LoggerFactory.getLogger(OpenAiService.class);
    private final ConfigProperties configProperties;

    public OpenAiService(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    private static final String prompt = "請幫我整理以上多個帳號的電子郵件資訊:\n" +
            "1.依照每封郵件的主題做主分類，寄件者是子分類。\n" +
            "2.寄件者(若有名稱顯示則顯示名稱，若沒有則顯示電子郵件地址)\n" +
            "3.產生內容摘要，以及建議的後續動作。\n" +
            "範例如下：\n" +
            "主類：活動通知\n" +
            "\n" +
            "寄件者：台灣岡波聖地\n" +
            "\n" +
            "行程內容摘要：關於修行的管理與實踐，強調從自心管理開始，修行如同耕作一片田地。\n" +
            "建議的後續動作：\n" +
            "若有興趣參與修行活動，查詢相關活動詳情並進行報名。\n" +
            "可考慮參與修行心得交流，增進自我管理能力。\n" +
            "寄件者：ACCUPASS 活動社交平台\n" +
            "\n" +
            "行程內容摘要：台電退役材料再生計畫展，展示台電老建築和退役材料，並設有文創商品展區。\n" +
            "建議的後續動作：\n" +
            "若對展覽感興趣，安排參觀時間並進行報名。\n" +
            "可考慮參加與展覽相關的講座或活動，了解更多背景信息。" +
            "主類：食材管理\n" +
            "\n" +
            "寄件者：pigmonkey0921929239@gmail.com\n" +
            "\n" +
            "行程內容摘要：共403筆食材記錄。\n" +
            "建議的後續動作：\n" +
            "檢查食材庫存及其有效期限，根據需要更新庫存資訊。";

    public ResultStatus<String> genUnreadMsgSummary() {
        ResultStatus<String> resultStatus = new ResultStatus<>();
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(configProperties.getOpenaiApiKey());

        String data = createPayload(readFile() + "\n" + prompt);

        HttpEntity<String> entity = new HttpEntity<>(data, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, ChatCompletionResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("OpenAI回傳的status code: {}", response);
            ChatCompletionResponse responseBody = response.getBody();
            String content = extractJsonContent(responseBody);
            System.out.println("GPT回傳資料 ======> " + content);

            resultStatus.setCode("C000");
            resultStatus.setMessage("成功");
            resultStatus.setData(content);
        } else {
            logger.error("openAI錯誤發生");
            resultStatus.setCode("C999");
            resultStatus.setMessage("異常發生");
        }
        return resultStatus;
    }

    private String readFile() {
        final String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sourceFilePath = "data/" + yesterday + ".txt";
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            logger.error("讀取檔案時發生錯誤: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    private String createPayload(String prompt) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-4o-mini");

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        payload.put("messages", new Object[]{message});

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractJsonContent(ChatCompletionResponse responseBody) {
        if (responseBody != null && !responseBody.getChoices().isEmpty()) {
            ChatCompletionResponse.Choice choice = responseBody.getChoices().get(0);
            if (choice != null && choice.getMessage() != null) {
                String content = choice.getMessage().getContent().trim();

                // 去掉反引號
                if (content != null) {
                    content = content.replace("```json", "").replace("```", "").trim();
                }

                return content;
            }
        }
        return null;
    }
}
