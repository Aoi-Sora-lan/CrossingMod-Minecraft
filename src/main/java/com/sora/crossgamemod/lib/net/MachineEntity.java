package com.sora.crossgamemod.lib.net;

import com.sora.crossgamemod.lib.base.IMachineLogic;
import com.sora.crossgamemod.lib.message.ItemPackage;
import com.sora.crossgamemod.lib.message.SetChannelRequest;

import java.util.concurrent.CompletableFuture;

public class MachineEntity {
    public MachineAddress address;
    public MessageHandler handler;
    private int nowMessageIndex = 0;
    private IMachineLogic logic;

    private CompletableFuture<Void> sendMessage(int type, int targetChannel, Object content) {
        Message message = new Message();
        message.messageType = type;
        message.sourceAddress = address.getMessageAddress(nowMessageIndex++);
        message.targetChannel = targetChannel;
        message.content = content;
        handler.sendMessage(message);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> sendRegisterMachineMessage() {
        return sendMessage(MessageType.RegisterMachine, -1, null);
    }

    public CompletableFuture<Void> sendSetChannelMessage(int channel, MachineIOType ioType) {
        SetChannelRequest request = new SetChannelRequest(ioType,channel);
        return sendMessage(MessageType.SetChannel, channel, request);
    }

    public CompletableFuture<Void> sendItemRequestMessage(int channel, String itemId, int itemCount) {
        ItemPackage pkg = new ItemPackage(itemId, itemCount);
        logic.preSend();
        return sendMessage(MessageType.ItemRequest, channel, pkg);
    }

    public MachineEntity(MachineAddress address, BaseUdpClient client, IMachineLogic logic) {
        this.address = address;
        this.logic = logic;
        this.handler = new MessageHandler(address, client, logic);
    }

    public void onConsumeMessage(Message receivedMessage) {
        handler.onConsumeMessage(receivedMessage);
    }

    public CompletableFuture<Void> sendSignal(int channel) {
        return sendMessage(MessageType.Signal, channel, null);
    }

    public CompletableFuture<Void> sendRemoveMachineMessage() {
        return sendMessage(MessageType.RemoveMachine, -1, null);
    }
}
