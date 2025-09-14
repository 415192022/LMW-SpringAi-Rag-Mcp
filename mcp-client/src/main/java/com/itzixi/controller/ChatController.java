package com.itzixi.controller;

import com.itzixi.bean.ChatEntity;
import com.itzixi.service.ChatService;
import com.itzixi.utils.LMWResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("chat")
public class ChatController {


    @Resource
    private ChatService chatService;

    @GetMapping("doChatTest")
    public String doChatTest( String msg){
       return chatService.chatTest(msg);
    }

    @GetMapping("streamResponse")
    public Flux<ChatResponse> streamResponse(String msg,HttpServletResponse response){
        return chatService.streamResponse(msg);
    }

    @GetMapping("streamStr")
    public Flux<String> streamStr(String msg, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        return chatService.streamStr(msg);
    }


    @PostMapping("doChat")
    public LMWResult doChat(@RequestBody ChatEntity chatEntity){
        System.out.println("1111111"+chatEntity.toString());
        chatService.doChat(chatEntity);
        return LMWResult.ok();
    }
}
