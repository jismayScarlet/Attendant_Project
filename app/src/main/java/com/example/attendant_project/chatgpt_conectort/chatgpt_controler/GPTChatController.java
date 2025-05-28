package com.example.attendant_project.chatgpt_conectort.chatgpt_controler;

@RestController
@RequestMapping("/api/chat")
public class GPTChatController {

    @PostMapping
    public ResponseEntity<Request_ResponseDTO.GPTResponse> chat(@RequestBody Request_ResponseDTO.GPTRequest request) {
        try {
            String reply = OpenAIService.sendMessage(
                    request.getSystemPrompt(),
                    request.getMessages()
            );
            return ResponseEntity.ok(new Request_ResponseDTO.GPTResponse(reply));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new Request_ResponseDTO.GPTResponse("錯誤：" + e.getMessage()));
        }
    }
}

