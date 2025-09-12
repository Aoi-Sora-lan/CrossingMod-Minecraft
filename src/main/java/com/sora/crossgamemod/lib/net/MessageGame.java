package com.sora.crossgamemod.lib.net;

import com.google.gson.annotations.SerializedName;

public final class MessageGame {
    @SerializedName("GameType")
    public String gameType;
    @SerializedName("GameId")
    public String gameId;

    public MessageGame(String gameType, String gameId) {
        this.gameType = gameType;
        this.gameId = gameId;
    }
}
