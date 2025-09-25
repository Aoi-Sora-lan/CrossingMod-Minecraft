package com.sora.crossgamemod.lib.base;

import com.sora.crossgamemod.lib.message.ItemPackage;
import com.sora.crossgamemod.lib.message.ItemResponse;

public interface IMachineLogic {
    public boolean CanTransfer(String itemId, int itemCount);
    int getMaxNeedCount(String itemId, int itemCount);
    void preSend();
    void sendSuccess(ItemResponse contentValue);
    void sendFailure();
    void generateItem(ItemPackage pkg);
    void onSignal();
}
