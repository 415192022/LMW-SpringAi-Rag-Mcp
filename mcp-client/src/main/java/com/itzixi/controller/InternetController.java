package com.itzixi.controller;

import com.itzixi.bean.ChatEntity;
import com.itzixi.service.ChatService;
import com.itzixi.service.SearXngService;
import com.itzixi.utils.LMWResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("internet")
public class InternetController {

    @Resource
    private SearXngService sesrXngService;

    @Resource
    private ChatService chatService;

    @GetMapping("/test")
    public Object test(@RequestParam("query") String query){
        return sesrXngService.search(query);
    }

    @PostMapping("/search")
    public LMWResult search(@RequestBody ChatEntity chatEntity, HttpServletResponse response){
        response.setCharacterEncoding("UTF-8");
        chatService.doInternetSearch(chatEntity);
        return LMWResult.ok();
    }

}
