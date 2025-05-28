package com.example.attendant_project.chatgpt_conectort.chatgpt_controler;

import java.util.List;

public class Request_ResponseDTO {
    public class GPTRequest {
        private String clientId;
        private String systemPrompt;
        private List<Message> messages;
        // getter/setter
    }

    public class Message {
        private String role; // system/user/assistant
        private String content;
        // getter/setter
    }

    public class GPTResponse {
        private String reply;
        public GPTResponse(String reply) { this.reply = reply; }
        // getter/setter
    }
}


