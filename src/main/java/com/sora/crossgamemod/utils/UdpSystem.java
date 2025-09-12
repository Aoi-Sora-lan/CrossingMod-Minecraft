package com.sora.crossgamemod.utils;

import com.sora.crossgamemod.Config;
import com.sora.crossgamemod.lib.base.IMachineLogic;
import com.sora.crossgamemod.lib.net.BaseUdpClient;
import com.sora.crossgamemod.lib.net.MachineAddress;
import com.sora.crossgamemod.lib.net.MachineEntity;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.UUID;

/**
 * 抽象的UDP消息收发类
 * 提供发送消息、启动UDP服务和关闭UDP服务的方法
 */
public class UdpSystem {
    public BaseUdpClient Client;
    private static UdpSystem _instance;
    private static String _gameId;
    private final String GameType = "Minecraft";

    public static UdpSystem get_instance(){
        if(_instance == null){
            _instance = new UdpSystem();
            _gameId = generateShortGuidId();
        }
        return _instance;
    }
    public void init() {
        try {
            Client = new BaseUdpClient(Config.crossLocalPort, Config.crossServerIp, Config.crossServerPort);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        Client.start();
    }
    public MachineEntity Register(IMachineLogic logic)
    {
        var address = new MachineAddress(GameType,_gameId,generateShortGuidId());
        var machineEntity = Client.register(address, logic);
        return machineEntity;
    }
    public void close()
    {
        Client.removeMachines();
        Client.close();
    }
    public static String generateShortGuidId() {
        // 获取 UUID 的字节数组
        UUID uuid = UUID.randomUUID();
        byte[] bytes = new byte[16];
        System.arraycopy(longToBytes(uuid.getMostSignificantBits()), 0, bytes, 0, 8);
        System.arraycopy(longToBytes(uuid.getLeastSignificantBits()), 0, bytes, 8, 8);

        // Base64 编码并替换特殊字符
        String base64 = Base64.getEncoder().encodeToString(bytes)
                .replace("/", "_")
                .replace("+", "-");

        // 截取前 10 个字符
        return base64.substring(0, 10);
    }

    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(value & 0xFF);
            value >>= 8;
        }
        return result;
    }
}
