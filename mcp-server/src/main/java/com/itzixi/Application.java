package com.itzixi;

import com.itzixi.mcptool.DateTool;
import com.itzixi.mcptool.EmailTool;
import com.itzixi.mcptool.ProductTool;
import com.itzixi.mcptool.SenderTool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.itzixi.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ToolCallbackProvider registerToolCallbackProvider(DateTool dateTool, EmailTool emailTool, ProductTool productTool,SenderTool senderTool) {
        return MethodToolCallbackProvider
                .builder()
                .toolObjects(dateTool,emailTool,productTool,senderTool)
                .build();
    }

}
