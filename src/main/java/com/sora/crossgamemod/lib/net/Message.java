package com.sora.crossgamemod.lib.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public class Message {
    @SerializedName("MessageType")
    public int messageType;
    @SerializedName("SourceAddress")
    public MessageAddress sourceAddress;
    @SerializedName("TargetAddress")
    public MessageAddress targetAddress;
    @SerializedName("TargetChannel")
    public int targetChannel;
    @SerializedName("Content")
    public Object content;

    public Message() {
        this.messageType = MessageType.None;
    }


    public <T> T getContent(Class<T> clazz) {
        if (content == null) {
            return null;
        }
        Gson gson = new Gson();
        if (content instanceof JsonObject) {
            return gson.fromJson((JsonObject) content, clazz);
        } else {
            // 当content已被Gson解析为LinkedTreeMap或其他Map时，先转成Json字符串，再反序列化
            String jsonStr = gson.toJson(content);
            return gson.fromJson(jsonStr, clazz);
        }
    }
}
