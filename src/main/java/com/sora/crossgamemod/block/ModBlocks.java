package com.sora.crossgamemod.block;

import com.sora.crossgamemod.CrossGameMod;
import com.sora.crossgamemod.block.custom.CrossingMachine;
import com.sora.crossgamemod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;


public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CrossGameMod.MODID);
    public static final RegistryObject<Block> CROSSING_MACHINE =
            registerBlock("crossing_machine",
                    () -> new CrossingMachine(BlockBehaviour.Properties.copy(Blocks.STONE).destroyTime(0.5f).strength(0.1f)));
    /*    public static final RegistryObject<Block> CROSSING_MACHINE = registerBlock("crossing_machine",
            () -> new CrossingMachineBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));*/
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
