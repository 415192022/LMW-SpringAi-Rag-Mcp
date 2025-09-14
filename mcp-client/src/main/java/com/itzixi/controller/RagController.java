package com.itzixi.controller;

import com.itzixi.bean.ChatEntity;
import com.itzixi.service.ChatService;
import com.itzixi.service.DocumentService;
import com.itzixi.utils.LMWResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("rag")
public class RagController {

    @Resource
    private DocumentService documentService;

    @Resource
    private ChatService chatService;

    @PostMapping("/uploadRagDoc")
    public LMWResult uploadRagDoc(@RequestParam("file") MultipartFile file ){
        List<Document> documentList =  documentService.loadText(file.getResource(), file.getOriginalFilename());
        return LMWResult.ok(documentList);
    }

    @GetMapping("/doSearch")
    public LMWResult doSearch(@RequestParam String question) {
        System.out.println(question);
        return LMWResult.ok(documentService.doSearch(question));
    }


    @PostMapping("/search")
    public LMWResult search(@RequestBody ChatEntity chatEntity, HttpServletResponse response) {
        System.out.println("===search====> "+chatEntity);
        List<Document> list = documentService.doSearch(chatEntity.getMessage());
        response.setCharacterEncoding("UTF-8");
        chatService.doChatRagSearch(chatEntity, list);
        return LMWResult.ok();
    }
}
