package com.sora.crossgamemod.lib.message;

import com.google.gson.annotations.SerializedName;

public final class ItemPackage {
    @SerializedName("ItemId")
    public String itemId;
    @SerializedName("ItemCount")
    public int itemCount;

    public ItemPackage(String itemId, int itemCount) {
        this.itemId = itemId;
        this.itemCount = itemCount;
    }
}
