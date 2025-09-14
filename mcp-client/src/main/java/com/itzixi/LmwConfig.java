package com.itzixi;

import com.itzixi.bean.ChatEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LmwConfig {

    @Bean
    public ChatEntity generateChatEntity() {
        return new ChatEntity("123","456","789");
    }
}
