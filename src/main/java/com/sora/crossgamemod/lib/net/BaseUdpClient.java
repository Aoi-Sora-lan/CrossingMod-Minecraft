package com.sora.crossgamemod.lib.net;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sora.crossgamemod.lib.base.IMachineLogic;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BaseUdpClient implements AutoCloseable {
    private DatagramSocket udpSocket;
    private InetSocketAddress remoteEndPoint;
    private ConcurrentLinkedQueue<UdpReceiveResult> receiveResults = new ConcurrentLinkedQueue<>();
    private List<MachineEntity> machineEntities = new ArrayList<>();
    private Map<MessageGame, InetSocketAddress> gameMapper = new HashMap<>();
    private Map<InetSocketAddress, MessageGame> ipMapper = new HashMap<>();
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Thread receiveThread;
    private Thread handleThread;

    public BaseUdpClient(int port, String remoteIp, int remotePort) throws SocketException, UnknownHostException {
        this.remoteEndPoint = new InetSocketAddress(InetAddress.getByName(remoteIp), remotePort);
        this.udpSocket = new DatagramSocket(port);
    }

    public void start() {
        isRunning.set(true);

        receiveThread = new Thread(this::receiveMessages);
        receiveThread.setDaemon(true);
        receiveThread.start();

        handleThread = new Thread(this::handleMessages);
        handleThread.setDaemon(true);
        handleThread.start();
    }

    public MachineEntity register(MachineAddress machineAddress, IMachineLogic logic) {
        MachineEntity entity = new MachineEntity(machineAddress, this, logic);
        machineEntities.add(entity);
        return entity;
    }

    private void handleMessages() {
        System.out.println("[DEBUG] Handle message thread started");

        while (isRunning.get()) {
            UdpReceiveResult result = receiveResults.poll();
            if (result == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    System.out.println("[INFO] Handle thread interrupted, exiting");
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }

            // 打印接收到的原始数据信息
            System.out.println("[DEBUG] Received UDP packet from: " + result.getRemoteEndPoint());
            System.out.println("[DEBUG] Packet length: " + result.getBuffer().length + " bytes");

            String receivedMessageStr = new String(result.getBuffer(), StandardCharsets.UTF_8);
            System.out.println("[DEBUG] Received raw string (" + receivedMessageStr.length() + " chars): " + receivedMessageStr);

            // 打印当前注册的机器实体数量
            System.out.println("[DEBUG] Current machineEntities size: " + machineEntities.size());

            Message receivedMessage = null;
            try {
                receivedMessage = JsonUtils.fromJson(receivedMessageStr, Message.class);
                System.out.println("[DEBUG] Successfully parsed JSON message");

                // 打印解析后的消息内容
                if (receivedMessage != null) {
                    System.out.println("[DEBUG] Parsed message type: " + receivedMessage.getClass().getSimpleName());

                    if (receivedMessage.targetAddress != null) {
                        System.out.println("[DEBUG] Target machine ID: " + receivedMessage.targetAddress.machineId);
                    } else {
                        System.out.println("[WARN] Message has null targetAddress");
                    }
                } else {
                    System.out.println("[WARN] Parsed message is null");
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to parse JSON: " + e.getMessage());
                e.printStackTrace();
                continue; // 跳过处理此条消息
            }

            if (receivedMessage == null || receivedMessage.targetAddress == null) {
                System.out.println("[WARN] Skipping message due to null value");
                continue;
            }

            // 查找匹配的机器实体
            boolean found = false;
            for (MachineEntity entity : machineEntities) {
                if (entity.address != null &&
                        entity.address.machineId != null &&
                        entity.address.machineId.equals(receivedMessage.targetAddress.machineId)) {

                    System.out.println("[DEBUG] Found matching entity for machineId: " + receivedMessage.targetAddress.machineId);
                    System.out.println("[DEBUG] Entity class: " + entity.getClass().getSimpleName());

                    try {
                        entity.onConsumeMessage(receivedMessage);
                        System.out.println("[DEBUG] Message processed successfully");
                    } catch (Exception e) {
                        System.err.println("[ERROR] Error processing message in entity: " + e.getMessage());
                        e.printStackTrace();
                    }

                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("[WARN] No MachineEntity found for machineId: " + receivedMessage.targetAddress.machineId);
                System.out.println("[DEBUG] Registered machine IDs: ");
                for (MachineEntity entity : machineEntities) {
                    if (entity.address != null && entity.address.machineId != null) {
                        System.out.println("  - " + entity.address.machineId);
                    } else {
                        System.out.println("  - [INVALID] Entity with null address or machineId");
                    }
                }
            }
        }

        System.out.println("[INFO] Handle message thread exiting");
    }
    public void removeMachine(MachineEntity entity)
    {
        entity.sendRemoveMachineMessage();
        machineEntities.remove(entity);
    }
    public void removeMachines()
    {
        for (var machineEntity:machineEntities) {
            machineEntity.sendRemoveMachineMessage();
        }
    }
    public void sendMessage(Message message) throws IOException {
        String jsonStr = JsonUtils.toJson(message);
        System.out.println(jsonStr);
        byte[] bytes = jsonStr.getBytes(StandardCharsets.UTF_8);
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, remoteEndPoint);
        udpSocket.send(packet);
    }

    private InetSocketAddress getEndPoint(MessageGame game) {
        return gameMapper.get(game);
    }

    private void receiveMessages() {
        byte[] buffer = new byte[4096];

        while (isRunning.get()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                byte[] receivedData = Arrays.copyOf(packet.getData(), packet.getLength());
                UdpReceiveResult result = new UdpReceiveResult(receivedData, packet.getSocketAddress());
                receiveResults.offer(result);
            } catch (SocketException e) {
                if (isRunning.get()) {
                    e.printStackTrace();
                }
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        isRunning.set(false);

        if (receiveThread != null) {
            receiveThread.interrupt();
        }
        if (handleThread != null) {
            handleThread.interrupt();
        }

        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }
}
class JsonUtils{
    private static Gson gson = null;
    private static void BuildGson(){
        gson = new GsonBuilder().create();
    }

    public static Message fromJson(String receivedMessageStr, Class<Message> messageClass) {
        if(gson == null) BuildGson();
        return gson.fromJson(receivedMessageStr,messageClass);
    }

    public static String toJson(Object src) {
        if(gson == null) BuildGson();
        return gson.toJson(src);
    }
}
class UdpReceiveResult {
    private byte[] buffer;
    private SocketAddress remoteEndPoint;

    public UdpReceiveResult(byte[] buffer, SocketAddress remoteEndPoint) {
        this.buffer = buffer;
        this.remoteEndPoint = remoteEndPoint;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public SocketAddress getRemoteEndPoint() {
        return remoteEndPoint;
    }
}
