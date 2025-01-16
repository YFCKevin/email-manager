package com.gurula.mailXpert.oauth2;

import com.gurula.mailXpert.security.ConfigProperties;
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
    private final ConfigProperties configProperties;

    public OauthLoginFailureHandler(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        logger.error("OAuth2 Login Failure: ", exception);
        logger.error("Authentication Exception Message: {}", exception.getMessage());
        response.sendRedirect(configProperties.getGlobalDomain() + "authorize.html?result=failed");
    }
}

