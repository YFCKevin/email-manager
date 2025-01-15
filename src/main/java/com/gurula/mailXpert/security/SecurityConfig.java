package com.gurula.mailXpert.security;

import com.gurula.mailXpert.oauth2.CustomOAuth2UserService;
import com.gurula.mailXpert.oauth2.OauthLoginFailureHandler;
import com.gurula.mailXpert.oauth2.OauthLoginSuccessHandler;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Resource
    private CustomOAuth2UserService oauthUserService;

    @Autowired
    ConfigProperties configProperties;

    @Resource
    OauthLoginSuccessHandler oauthLoginSuccessHandler;  //第三方登入成功後會處理的

    @Resource
    OauthLoginFailureHandler oauthLoginFailureHandler;  //第三方登入失敗或取消會進來處理

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().disable();
        http.headers().frameOptions().disable();
        http.csrf(csrf -> csrf.disable());

        http.logout()
                .and().oauth2Login()
                .clientRegistrationRepository(clientRegistrationRepository())
                .userInfoEndpoint()
                .userService(oauthUserService)
                .and()
                .successHandler(oauthLoginSuccessHandler)
                .failureHandler(oauthLoginFailureHandler);

        return http.build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {

        ClientRegistration googleClientRegistration1 = ClientRegistration.withRegistrationId("google1")
                .clientId(configProperties.getClientId1())
                .clientSecret(configProperties.getClientSecret1())
                .scope("email",
                        "profile",
                        "https://www.googleapis.com/auth/gmail.readonly",
                        "https://www.googleapis.com/auth/gmail.compose",
                        "https://www.googleapis.com/auth/gmail.send")
                .redirectUri(configProperties.getRedirectUri1())
                .authorizationUri("https://accounts.google.com/o/oauth2/auth?access_type=offline")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .clientName("Google1")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .userNameAttributeName("sub")
                .build();

        ClientRegistration googleClientRegistration2 = ClientRegistration.withRegistrationId("google2")
                .clientId(configProperties.getClientId2())
                .clientSecret(configProperties.getClientSecret2())
                .scope("email",
                        "profile",
                        "https://www.googleapis.com/auth/gmail.readonly",
                        "https://www.googleapis.com/auth/gmail.compose",
                        "https://www.googleapis.com/auth/gmail.send")
                .redirectUri(configProperties.getRedirectUri2())
                .authorizationUri("https://accounts.google.com/o/oauth2/auth?access_type=offline")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .clientName("Google2")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .userNameAttributeName("sub")
                .build();

        return new InMemoryClientRegistrationRepository(googleClientRegistration2, googleClientRegistration1);
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

}
