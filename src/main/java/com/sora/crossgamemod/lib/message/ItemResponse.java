package com.sora.crossgamemod.lib.message;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public final class ItemResponse {
    @SerializedName("IsSuccess")
    public boolean isSuccess;
    @SerializedName("Reason")
    @Nullable
    public String reason;
    @SerializedName("ItemCount")
    public int itemCount;
    @SerializedName("ItemId")
    @Nullable
    public String itemId;

    public ItemResponse(boolean isSuccess, String reason, int itemCount, String itemId) {
        this.isSuccess = isSuccess;
        this.reason = reason;
        this.itemCount = itemCount;
        this.itemId = itemId;
    }
}
