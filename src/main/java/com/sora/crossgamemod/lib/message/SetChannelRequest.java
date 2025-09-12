package com.sora.crossgamemod.lib.message;

import com.google.gson.annotations.SerializedName;
import com.sora.crossgamemod.lib.net.MachineIOType;

public final class SetChannelRequest {
    @SerializedName("IOType")
    public MachineIOType ioType;
    @SerializedName("ChannelId")
    public int channelId;

    public SetChannelRequest(MachineIOType ioType, int channelId) {
        this.ioType = ioType;
        this.channelId = channelId;
    }
}
