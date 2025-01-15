package com.gurula.mailXpert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class MailXpertApplication {

	public static void main(String[] args) {
		SpringApplication.run(MailXpertApplication.class, args);
	}

    @Bean(name = "sdf")
    public SimpleDateFormat sdf () {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
        return sdf;
    }
}
