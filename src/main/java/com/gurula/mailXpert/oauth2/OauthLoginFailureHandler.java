package com.gurula.mailXpert.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *  第三方登入失敗或取消會進來處理
 */
@Component
public class OauthLoginFailureHandler implements AuthenticationFailureHandler {
    protected Logger logger = LoggerFactory.getLogger(OauthLoginFailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 記錄錯誤類型
        logger.error("OAuth2 Login Failure: ", exception);

        // 可以打印錯誤信息，來了解發生了什麼錯誤
        logger.error("Authentication Exception Message: {}", exception.getMessage());

        // 顯示堆棧跟蹤 (如果需要)
        exception.printStackTrace();

        // 進一步處理，例如向前端返回錯誤響應
        response.sendRedirect("/login?error=true");  // 例如，返回到登入頁面並顯示錯誤
    }
}

