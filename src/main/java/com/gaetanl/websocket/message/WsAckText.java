package com.gaetanl.websocket.message;

public class WsAckText extends WsMessage {
    private static final long serialVersionUID = -4377008034679793325L;

    private String originalText;

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalMsg) {
        this.originalText = originalMsg;
    }

    @Override
    public String getContent() {
        return String.format("Message «%s» well received", this.originalText);
    }
}
