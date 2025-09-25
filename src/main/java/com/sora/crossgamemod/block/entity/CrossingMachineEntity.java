package com.sora.crossgamemod.block.entity;
import com.sora.crossgamemod.lib.base.IMachineLogic;
import com.sora.crossgamemod.lib.message.ItemPackage;
import com.sora.crossgamemod.lib.message.ItemResponse;
import com.sora.crossgamemod.lib.net.BaseUdpClient;
import com.sora.crossgamemod.lib.net.MachineEntity;
import com.sora.crossgamemod.lib.net.MachineIOType;
import com.sora.crossgamemod.screen.CrossingMachineMenu;
import com.sora.crossgamemod.utils.UdpSystem;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CrossingMachineEntity extends BlockEntity implements MenuProvider, IMachineLogic {
    private ItemStack _tempSendItem = null;
    public MachineEntity UdpEntity;
    public int channel;
    public MachineIOType ioType = MachineIOType.None;
    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 10;
    private String voidItemID = "";
    private int crossMode;

    public CrossingMachineEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.CROSSING_MACHINE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> CrossingMachineEntity.this.progress;
                    case 1 -> CrossingMachineEntity.this.maxProgress;
                    case 2 -> CrossingMachineEntity.this.crossMode;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> CrossingMachineEntity.this.progress = pValue;
                    case 1 -> CrossingMachineEntity.this.maxProgress = pValue;
                    case 2 -> CrossingMachineEntity.this.crossMode = pValue;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public ItemStack getRenderStack() {
        if(itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty()) {
            return itemHandler.getStackInSlot(INPUT_SLOT);
        } else {
            return itemHandler.getStackInSlot(OUTPUT_SLOT);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        if(level == null) return;
        if(!level.isClientSide) {
            UdpEntity = UdpSystem.get_instance().Register(this);
            CompletableFuture.runAsync(() -> {
                UdpEntity.sendRegisterMachineMessage()
                        .thenCompose(unused -> UdpEntity.sendSetChannelMessage(channel, ioType))
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            return null;
                        });
            });
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    public void onRemove() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
        UdpSystem.get_instance().Client.removeMachine(UdpEntity);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.crossgamemod.crossing_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new CrossingMachineMenu(pContainerId, pPlayerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        pTag.put("inventory", itemHandler.serializeNBT());
        pTag.putInt("crossing_machine.channel", channel);
        pTag.putInt("crossing_machine.ioType", ioType.ordinal());
        pTag.putInt("crossing_machine.progress", progress);
        super.saveAdditional(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        itemHandler.deserializeNBT(pTag.getCompound("inventory"));
        progress = pTag.getInt("crossing_machine.progress");
        channel = pTag.getInt("crossing_machine.channel");
        ioType = MachineIOType.values()[pTag.getInt("crossing_machine.ioType")];
    }


    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {

        if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(pLevel, pPos, pState);

            if(hasProgressFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
    }
    private boolean hasRecipe() {
       /* Optional<GemPolishingRecipe> recipe = getCurrentRecipe();

        if(recipe.isEmpty()) {
            return false;
        }
        ItemStack result = recipe.get().getResultItem(getLevel().registryAccess());

        return canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
   */   //return !this.itemHandler.getStackInSlot(INPUT_SLOT).isEmpty();
        return !itemHandler.getStackInSlot(INPUT_SLOT).isEmpty();
    }
    private void craftItem() {
        TryRequest();
    }

    private Item getItem(String itemId){
        if (itemId == null) return null;
        ResourceLocation itemLocation = ResourceLocation.tryParse(itemId);
        System.out.println(itemLocation);
        if (itemLocation == null) return null;
        return ForgeRegistries.ITEMS.getValue(itemLocation);
    }
    private boolean canInsertItemIntoOutputSlot(Item item) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || this.itemHandler.getStackInSlot(OUTPUT_SLOT).is(item);
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + count <= this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private boolean hasProgressFinished() {
        return progress >= maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    public void receiveItem(String receivedMessage) {

        var args = receivedMessage.split("/");
        if(Objects.equals(args[1], "request")){
            var item = getItem(args[2]);
            if(item == null) {
                return;
            }
            ItemStack result = new ItemStack(item,1);
            var canInsert = canInsertAmountIntoOutputSlot(result.getCount()) && canInsertItemIntoOutputSlot(result.getItem());
            if(!canInsert){
                return;
            }
        }
        else if(Objects.equals(args[1], "transfer")){
            voidItemID = args[2];
        }
    }

    public void setData(int channelIndex, int ioType) {
        if (!BaseUdpClient.judgeOnline()) return;
        this.channel = channelIndex;
        this.ioType = MachineIOType.values()[ioType];
        if(level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3); // 新增
        }
        UdpEntity.sendSetChannelMessage(this.channel,this.ioType);

        this.setChanged(); // 标记为需要保存
    }
    private void TryRequest()
    {
        var item = itemHandler.getStackInSlot(INPUT_SLOT).getItem();
        var itemId = ForgeRegistries.ITEMS.getKey(item).toString();
        if(itemId == null) return;
        UdpEntity.sendItemRequestMessage(channel, itemId, itemHandler.getStackInSlot(INPUT_SLOT).getCount());
    }
    public boolean doesItemExist(String itemId) {
        try {
            // 将字符串转换为 ResourceLocation
            ResourceLocation resourceLocation = new ResourceLocation(itemId);

            // 检查注册表中是否存在该物品
            return ForgeRegistries.ITEMS.containsKey(resourceLocation);
        } catch (ResourceLocationException e) {
            // 如果字符串格式无效（如包含非法字符）
            return false;
        }
    }
    @Override
    public boolean CanTransfer(String itemId, int itemCount) {
        if(!doesItemExist(itemId)) return false;
        var outputSlot = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (outputSlot.isEmpty()) return true;
        var itemName = ForgeRegistries.ITEMS.getKey(outputSlot.getItem()).toString();
        var idDiff = itemId != itemName;
        if (idDiff) return false;
        var transCount = Math.max(0, itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize() - itemHandler.getStackInSlot(OUTPUT_SLOT).getCount());
        return transCount > 0;
    }

    @Override
    public int getMaxNeedCount(String itemId, int itemCount) {
        var itemNow = itemHandler.getStackInSlot(OUTPUT_SLOT);
        if (!itemNow.isEmpty()) return Math.max(0, itemNow.getMaxStackSize() - itemNow.getCount());
        var item = getItem(itemId);
        ItemStack result = new ItemStack(item,itemCount);
        return Math.max(0, result.getMaxStackSize());
    }

    @Override
    public void preSend() {
        if (!BaseUdpClient.judgeOnline()) return;
        _tempSendItem = itemHandler.getStackInSlot(INPUT_SLOT).copyAndClear();
    }

    @Override
    public void sendSuccess(ItemResponse contentValue) {
        var left = _tempSendItem.getCount() - contentValue.itemCount;
        if (left > 0)
        {
            itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(_tempSendItem.getItem(), left));
        }
        _tempSendItem.setCount(0);
    }

    @Override
    public void sendFailure() {
        itemHandler.setStackInSlot(OUTPUT_SLOT, _tempSendItem.copyAndClear());
    }

    @Override
    public void generateItem(ItemPackage pkg) {
        var item = getItem(pkg.itemId);
        ItemStack result = new ItemStack(item,pkg.itemCount);
        this.itemHandler.setStackInSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    @Override
    public void onSignal() {
        level.scheduleTick(getBlockPos(), getBlockState().getBlock(), 2);
    }


    public void sendSignal() {
        if (!BaseUdpClient.judgeOnline()) return;
        UdpEntity.sendSignal(channel);
    }
}
