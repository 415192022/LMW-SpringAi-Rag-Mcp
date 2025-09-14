package com.itzixi.mcptool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SenderTool {
    @Tool(description = "给指定的用户名发送消息，userId 为用户名，message 为发送的消息内容")
    public void sendMessageToUser(MessageRequest messageRequest) {
        log.info("MCP ===> sendMessageToUser");
        log.info(String.format("========== 参数 userId：%s ==========", messageRequest.userId));
        log.info(String.format("========== 参数 message：%s ==========", messageRequest.message));
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class  MessageRequest {
        @ToolParam(description = "消息接收的用户名")
        private String userId;
        @ToolParam(description = "发送消息的内容")
        private String message;
    }
}
