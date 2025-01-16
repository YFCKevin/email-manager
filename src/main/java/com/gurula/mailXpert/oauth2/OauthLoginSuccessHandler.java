package com.gurula.mailXpert.oauth2;

import com.gurula.mailXpert.security.ConfigProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OauthLoginSuccessHandler implements AuthenticationSuccessHandler {
    protected Logger logger = LoggerFactory.getLogger(OauthLoginSuccessHandler.class);
    private final UserService userService;
    private final OAuthTokenRepository oauthTokenRepository;
    private final ConfigProperties configProperties;
    @Autowired
    @Lazy
    private OAuth2AuthorizedClientService authorizedClientService;

    public OauthLoginSuccessHandler(UserService userService, OAuthTokenRepository oauthTokenRepository, ConfigProperties configProperties) {
        this.userService = userService;
        this.oauthTokenRepository = oauthTokenRepository;
        this.configProperties = configProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        System.out.println("第三方登入成功後要做的");

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();
        System.out.println(oauthUser.getOauth2ClientName());

        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // 使用 OAuth2AuthorizedClientService 獲取 OAuth2AuthorizedClient
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (authorizedClient != null) {
                // access_token
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                String tokenValue = accessToken.getTokenValue();
                System.out.println("Access Token: " + tokenValue);

                // refresh_token
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                if (refreshToken != null) {
                    String refreshTokenValue = refreshToken.getTokenValue();
                    System.out.println("Refresh Token: " + refreshTokenValue);
                } else {
                    System.out.println("沒有返回 refresh_token");
                }
                saveOAuthToken(oauthUser.getEmail(), oauthUser.getOauth2ClientName(), tokenValue, refreshToken != null ? refreshToken.getTokenValue() : null);
                response.sendRedirect(configProperties.getGlobalDomain() + "authorize.html?result=success");
            }
        }
    }


    private void saveOAuthToken(String userId, String oauth2ClientName, String accessToken, String refreshToken) {
        Optional<OAuthToken> existingToken = oauthTokenRepository.findByUserIdAndOauth2ClientName(userId, oauth2ClientName);
        final OAuthToken oAuthToken;
        if (existingToken.isPresent()) {
            oAuthToken = existingToken.get();
            if (!accessToken.equals(oAuthToken.getAccessToken()) && refreshToken != null) {
                oAuthToken.setRefreshToken(refreshToken);
            }
            oAuthToken.setAccessToken(accessToken);
        } else {
            oAuthToken = new OAuthToken();
            oAuthToken.setUserId(userId);
            oAuthToken.setOauth2ClientName(oauth2ClientName);
            oAuthToken.setAccessToken(accessToken);
            oAuthToken.setRefreshToken(refreshToken);
        }
        oAuthToken.setTokenExpiry(System.currentTimeMillis() + (60 * 60));   // １hr
        oauthTokenRepository.save(oAuthToken);
    }
}
