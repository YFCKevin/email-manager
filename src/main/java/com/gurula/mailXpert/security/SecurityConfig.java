package com.gurula.mailXpert.security;

import com.gurula.mailXpert.oauth2.*;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Resource
    Oauth2Config oauth2Config;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers().disable();
        http.headers().frameOptions().disable();
        http.csrf(csrf -> csrf.disable());

        http.logout()
                .and().oauth2Login()
                .clientRegistrationRepository(oauth2Config.clientRegistrationRepository())
                .userInfoEndpoint()
                .userService(oauthUserService)
                .and()
                .successHandler(oauthLoginSuccessHandler)
                .failureHandler(oauthLoginFailureHandler);

        return http.build();
    }
}
