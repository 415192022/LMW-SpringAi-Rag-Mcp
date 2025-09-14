package com.itzixi.bean;

import lombok.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChatEntity {

    private String currentUserName;
    private String message;
    private String botMsgId;

}
