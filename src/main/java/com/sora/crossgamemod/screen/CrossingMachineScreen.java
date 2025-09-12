package com.sora.crossgamemod.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.sora.crossgamemod.CrossGameMod;
import com.sora.crossgamemod.lib.net.MachineIOType;
import com.sora.crossgamemod.utils.ModPacketHandler;
import com.sora.crossgamemod.utils.UpdateBlockEntityPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrossingMachineScreen extends AbstractContainerScreen<CrossingMachineMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(CrossGameMod.MODID, "textures/gui/crossing_machine_gui.png");

    public CrossingMachineScreen(CrossingMachineMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        this.addWidget(Button
                .builder(Component.translatable("screen.crossgamemod.crossing_machine"),
                button->
                {
                    var nChannel = menu.getChannel() - 1;
                    if(nChannel < 0) nChannel = 0;
                    UpdateBlockEntityPacket packet =
                            new UpdateBlockEntityPacket(menu.getBlockPos(),nChannel, menu.getIoType().ordinal());
                    ModPacketHandler.INSTANCE.sendToServer(packet);
                }).pos(x+10,y+31).size(20,22).build());
        this.addWidget(Button
                .builder(Component.translatable("screen.crossgamemod.crossing_machine"),
                        button->
                        {
                            var entity = this.menu.blockEntity;
                            UpdateBlockEntityPacket packet =
                                    new UpdateBlockEntityPacket(menu.getBlockPos(),menu.getChannel() + 1, menu.getIoType().ordinal());
                            ModPacketHandler.INSTANCE.sendToServer(packet);
                        }).pos(x+10,y+4).size(20,22).build());
        this.addWidget(Button
                .builder(Component.translatable("screen.crossgamemod.crossing_machine"),
                        button->
                        {
                            var entity = this.menu.blockEntity;
                            UpdateBlockEntityPacket packet =
                                    new UpdateBlockEntityPacket(entity.getBlockPos(), entity.channel,(entity.ioType.ordinal()+1)%3);
                            ModPacketHandler.INSTANCE.sendToServer(packet);
                        }).pos(x+10,y+58).size(20,22).build());
    }
    private void renderStatusText(GuiGraphics guiGraphics, int x, int y) {
        int channel = this.menu.getChannel();
        MachineIOType ioType = this.menu.getIoType();

        // 渲染频道信息
        String channelText = "Channel: " + channel;
        guiGraphics.drawString(this.font, channelText, x + 35, y + 15, 0xFFFFFF);

        // 渲染IO类型信息
        String ioTypeText = "Mode: " + getIoTypeDisplayName(ioType);
        guiGraphics.drawString(this.font, ioTypeText, x + 35, y + 65, 0xFFFFFF);
    }

    private String getIoTypeDisplayName(MachineIOType ioType) {
        switch (ioType) {
            case Input: return "Input";
            case Output: return "Output";
            case None: return "Disabled";
            default: return "Unknown";
        }
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderStatusText(guiGraphics, x, y);
        renderButton(guiGraphics, x, y);
        renderButtonA(guiGraphics, x, y);
        renderButtonB(guiGraphics, x, y);
        renderProgressArrow(guiGraphics, x, y);
    }

    private void renderButton(GuiGraphics guiGraphics, int x, int y){
        guiGraphics.blit(TEXTURE, x + 10, y + 31, 192, 0, 20,22);
        guiGraphics.blit(TEXTURE, x+12,y+33,menu.isRedStone() ? 208:192 ,32,16,16);
    }
    private void renderButtonA(GuiGraphics guiGraphics, int x, int y){
        guiGraphics.blit(TEXTURE, x + 10, y + 4, 192, 0, 20,22);
        guiGraphics.blit(TEXTURE, x+12,y+6,menu.isRedStone() ? 208:192 ,32,16,16);
    }
    private void renderButtonB(GuiGraphics guiGraphics, int x, int y){
        guiGraphics.blit(TEXTURE, x + 10, y + 58, 192, 0, 20,22);
        guiGraphics.blit(TEXTURE, x+12,y+60,menu.isRedStone() ? 208:192 ,32,16,16);
    }
    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(TEXTURE, x + 85, y + 30, 176, 0, 8, menu.getScaledProgress());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
