package com.sora.crossgamemod.lib.net;

import com.sora.crossgamemod.lib.base.IMachineLogic;
import com.sora.crossgamemod.lib.message.ItemPackage;
import com.sora.crossgamemod.lib.message.ItemResponse;
import com.sora.crossgamemod.lib.message.Response;
import com.sora.crossgamemod.lib.net.Message;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class MessageHandler {
    private MachineAddress hostAddress;
    private IMachineLogic logic;
    private BaseUdpClient client;
    private Map<Integer, CompletableFuture<Message>> waitForResponseMessages = new ConcurrentHashMap<>();
    private MessageBuilder messageBuilder = new MessageBuilder();
    private static final Logger Log = Logger.getLogger(MessageHandler.class.getName());

    public MessageHandler(MachineAddress address, BaseUdpClient client, IMachineLogic logic) {
        this.hostAddress = address;
        this.client = client;
        this.logic = logic;
    }

    public void onConsumeMessage(Message message) {
        logReceiveMessage(message);
        switch (message.messageType) {
            case MessageType.None:
                break;
            case MessageType.Response:
                handleResponse(message);
                break;
            case MessageType.ItemRequest:
                handleItemRequest(message);
                break;
            case MessageType.ItemResponse:
                handleItemResponse(message);
                break;
            case MessageType.Transfer:
                handleTransfer(message);
                break;
            case MessageType.Signal:
                handleSignal();
                break;
            case MessageType.RegisterMachine:
                handleRegisterMachine(message);
                break;
            default:
                throw new IllegalArgumentException("Invalid message type");
        }
    }

    private void handleSignal() {
        logic.onSignal();
    }

    private void handleTransfer(Message message) {
        ItemPackage content = message.getContent(ItemPackage.class);
        if (content == null) return;
        logic.generateItem(content);
    }

    private void handleItemResponse(Message itemResponse) {
        ItemResponse content = itemResponse.getContent(ItemResponse.class);
        if (content == null) return;
        logItemResponseMessage(content);

        if (!content.isSuccess) {
            logic.sendFailure();
            return;
        }

        logic.sendSuccess(content);
        sendMessage(messageBuilder
                .copy(itemResponse)
                .reverseAddress()
                .setType(MessageType.Transfer)
                .setContent(new ItemPackage(content.itemId, content.itemCount))
                .build());
    }

    private void handleResponse(Message response) {
        if (response.getContent(Response.class).isSuccess)
        {
            BaseUdpClient.IsOnline = true;
        }
    }

    public void sendRegister() {
    }

    public void sendMessage(Message message) {
        logSendMessage(message);
        if (message.content instanceof ItemPackage) {
            logItemPackage((ItemPackage) message.content);
        }
        if (message.content instanceof ItemResponse) {
            logItemResponseMessage((ItemResponse) message.content);
        }
        try {
            client.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateMachineId() {
        return UUID.randomUUID().toString();
    }

    private void handleRegisterMachine(Message register) {
        // Implementation remains empty as in original
    }

    private void handleItemRequest(Message itemRequest) {
        ItemPackage content = itemRequest.getContent(ItemPackage.class);
        logItemPackage(content);

        boolean canTransfer = logic.CanTransfer(content.itemId, content.itemCount);
        if (canTransfer) {
            int maxNeed = logic.getMaxNeedCount(content.itemId, content.itemCount);
            int count = Math.min(maxNeed, content.itemCount);
            sendMessage(messageBuilder
                    .copy(itemRequest)
                    .reverseAddress()
                    .setType(MessageType.ItemResponse)
                    .setContent(new ItemResponse(true, null, count, content.itemId))
                    .build());
        } else {
            sendMessage(messageBuilder
                    .copy(itemRequest)
                    .reverseAddress()
                    .setType(MessageType.ItemResponse)
                    .setContent(new ItemResponse(false, "output blocked", 0,null ))
                    .build());
        }
    }

    public CompletableFuture<Message> waitingFor(Message request) {
        CompletableFuture<Message> future = new CompletableFuture<>();
        // waitForResponseMessages.put(request.messageId, future);
        return future;
    }

    // Logging methods
    private void logReceiveMessage(Message message) {
        Log.info(String.format("[%s]收到了来自%s的%s消息",
                hostAddress.machineId, message.sourceAddress, message.messageType));
    }

    private void logItemPackage(ItemPackage pkg) {
        Log.info(String.format("[%s]消息内容为：%d个%s物品",
                hostAddress.machineId, pkg.itemCount, pkg.itemId));
    }

    private void logItemResponseMessage(ItemResponse response) {
        if (response.isSuccess) {
            Log.info(String.format("[%s]消息内容为：成功！将转换%d个%s物品",
                    hostAddress.machineId, response.itemCount, response.itemId));
        } else {
            Log.info(String.format("[%s]消息内容为：失败！原因是:%s",
                    hostAddress.machineId, response.reason));
        }
    }

    private void logSendMessage(Message message) {
        Log.info(String.format("[%s]发送了去往%d号频道的%s消息",
                hostAddress.machineId, message.targetChannel, message.messageType));
    }
}
