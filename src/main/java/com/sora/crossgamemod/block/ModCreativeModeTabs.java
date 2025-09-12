package com.sora.crossgamemod.block;

import com.sora.crossgamemod.CrossGameMod;
import com.sora.crossgamemod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrossGameMod.MODID);

    public static final RegistryObject<CreativeModeTab> CROSSGAME_TAB = CREATIVE_MODE_TABS
            .register("crossgame_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.translatable("creativetab.crossgame_tab"))
            .icon(() -> Items.ENDER_EYE.getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.CROSSING_MACHINE.get());
            }).build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
