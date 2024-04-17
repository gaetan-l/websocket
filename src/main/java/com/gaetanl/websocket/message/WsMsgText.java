package com.gaetanl.websocket.message;

public class WsMsgText extends WsMessage {
	private static final long serialVersionUID = -955345244784970404L;

	String text;

	@Override
	public WsAckText getAck() {
		WsAckText ack = new WsAckText();
		ack.setOriginalText(this.text);
		return ack;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getContent() {
		return text;
	}
}