package com.gaetanl.websocket.message;

import java.io.Serializable;
import java.util.Date;

public abstract class WsMessage implements Serializable {
    private static final long serialVersionUID = 4764954821836307569L;

    final protected String type;
    final protected Date creationTime;

    public WsMessage() {
        this.type = this.getClass().getName();
        this.creationTime = new Date();
    }

    /**
     * Retourne l'accusé de réception correspondant au présent message.
     *
     * @return  l'accusé de réception, ou null s'il n'y en a pas pour ce type
     *          de message
     */
    public WsMessage getAck() {
        return null;
    }

    @Override
    public String toString() {
        return "WsTestMessage [content= " + getContent() + "]";
    }

    public String getType() {
        return type;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public abstract String getContent();
}
