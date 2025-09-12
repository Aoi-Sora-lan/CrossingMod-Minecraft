package com.sora.crossgamemod.lib.net;

import com.google.gson.annotations.SerializedName;

public final class MessageAddress {
    @SerializedName("GameType")
    public String gameType;
    @SerializedName("GameId")
    public String gameId;
    @SerializedName("MachineId")
    public String machineId;
    @SerializedName("MessageIndex")
    public int messageIndex;

    public MessageAddress(String gameType, String gameId, String machineId, int messageIndex) {
        this.gameType = gameType;
        this.gameId = gameId;
        this.machineId = machineId;
        this.messageIndex = messageIndex;
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%s/%d", gameType, gameId, machineId, messageIndex);
    }

    public MachineAddress getMachineAddress() {
        return new MachineAddress(gameType, gameId, machineId);
    }
}

