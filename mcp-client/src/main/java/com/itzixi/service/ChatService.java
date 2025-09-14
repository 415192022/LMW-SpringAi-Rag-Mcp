package com.itzixi.service;

import com.itzixi.bean.ChatEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatService {

    public String chatTest(String prompt);


    public Flux<ChatResponse> streamResponse(String prompt);

    public Flux<String> streamStr(String prompt);

    public void doChat(ChatEntity chatEntity);

    public void doChatRagSearch(ChatEntity chatEntity, List<Document> ragContext);

    public void doInternetSearch(ChatEntity chatEntity);
}
