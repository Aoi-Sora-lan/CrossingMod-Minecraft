package com.sora.crossgamemod.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

// 文件：ModPacketHandler.java
public class ModPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("mymodid", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0; // 用于生成唯一的包ID

    public static void register() {
        // 注册我们需要的包类型。这里我们注册一个用于更新BlockEntity的包。
        INSTANCE.registerMessage(
                packetId++,
                UpdateBlockEntityPacket.class, // 这是下一步要创建的包类
                UpdateBlockEntityPacket::encode, // 编码方法
                UpdateBlockEntityPacket::new, // 解码构造函数 (FriendlyByteBuf -> Packet)
                UpdateBlockEntityPacket::handle // 处理方法
        );
        // 可以在这里注册更多类型的包...
    }
}