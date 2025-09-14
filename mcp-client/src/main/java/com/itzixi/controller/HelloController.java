package com.itzixi.controller;

import com.itzixi.service.ChatService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("hello")
public class HelloController {


    @Resource
    private ChatService chatService;


    @GetMapping("world")
    public String world(){
        System.out.println("11111");
        return "Hello LMW!";
    }

    @GetMapping("doChat")
    public String doChat( String msg){
       return chatService.chatTest(msg);
    }
}
