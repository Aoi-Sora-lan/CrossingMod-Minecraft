package com.sora.crossgamemod.lib.net;

import com.google.gson.annotations.SerializedName;

public final class MachineAddress {
    @SerializedName("GameType")
    public String gameType;
    @SerializedName("GameId")
    public String gameId;
    @SerializedName("MachineId")

    public String machineId;

    public MachineAddress(String gameType, String gameId, String machineId) {
        this.gameType = gameType;
        this.gameId = gameId;
        this.machineId = machineId;
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%s", gameType, gameId, machineId);
    }

    public MessageAddress getMessageAddress(int messageIndex) {
        return new MessageAddress(gameType, gameId, machineId, messageIndex);
    }
}
