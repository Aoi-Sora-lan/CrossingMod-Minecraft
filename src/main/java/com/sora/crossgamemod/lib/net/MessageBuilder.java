package com.sora.crossgamemod.lib.net;

public class MessageBuilder {
    private Message message;

    public MessageBuilder setType(int type) {
        message.messageType = type;
        return this;
    }

    public MessageBuilder reverseAddress() {
        MessageAddress temp = message.sourceAddress;
        message.sourceAddress = message.targetAddress;
        message.targetAddress = temp;
        return this;
    }

    public MessageBuilder copy(Message message) {
        this.message = message;
        return this;
    }

    public MessageBuilder setContent(Object content) {
        message.content = content;
        return this;
    }

    public Message build() {
        return message;
    }
}
