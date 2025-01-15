package com.gurula.mailXpert.oauth2;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class UserService {
    private final SimpleDateFormat sdf;

    public UserService(@Qualifier("sdf") SimpleDateFormat sdf) {
        this.sdf = sdf;
    }

    public String processOAuthPostLogin(String email, String name, String oauth2ClientName) {

        return email;
    }

}
