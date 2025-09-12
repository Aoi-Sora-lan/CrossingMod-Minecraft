package com.sora.crossgamemod.utils;

import com.sora.crossgamemod.block.entity.CrossingMachineEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// 文件：UpdateBlockEntityPacket.java
public class UpdateBlockEntityPacket {
    private final BlockPos pos; // 方块实体的位置
    private final int channelIndex; // 要设置的新数据
    private final int ioType; // 要设置的新数据

    // 构造函数 - 用于客户端创建要发送的包
    public UpdateBlockEntityPacket(BlockPos pos, int channelIndex, int ioType) {
        this.pos = pos;
        this.channelIndex = channelIndex;
        this.ioType = ioType;
    }

    // 构造函数 - 用于从字节缓冲区解码（服务端接收时调用）
    public UpdateBlockEntityPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.channelIndex = buf.readInt();
        this.ioType = buf.readInt();
    }

    // 编码 - 将包的数据写入字节缓冲区（客户端发送时调用）
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.channelIndex);
        buf.writeInt(this.ioType);
    }

    // 处理 - 在服务端收到包时执行
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 1. 获取发送包的玩家
            ServerPlayer sender = context.getSender();
            // 2. 获取服务器上的世界对象
            Level level = sender.serverLevel();

            // !!! 重要：安全检查 !!!
            // 确保目标区块已加载，防止客户端恶意请求加载未加载的区块
            if (level.hasChunkAt(this.pos)) {
                // 3. 获取目标位置的BlockEntity
                BlockEntity blockEntity = level.getBlockEntity(this.pos);
                // 4. 检查是否是我们想要的特定类型的BlockEntity
                if (blockEntity instanceof CrossingMachineEntity myBE) { // 替换 MyBlockEntity 为你的实际类
                    // 5. 执行实际的数据更新
                    myBE.setData(this.channelIndex, this.ioType);
                    // 6. 标记BlockEntity为已更改，以便保存数据
                    myBE.setChanged();
                    // （可选）通知客户端更新，可以在这里发一个包回去
                    // ModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(sender), new ResponsePacket(...));
                }
            }
        });
        context.setPacketHandled(true); // 告诉系统包已处理完毕
    }
}
