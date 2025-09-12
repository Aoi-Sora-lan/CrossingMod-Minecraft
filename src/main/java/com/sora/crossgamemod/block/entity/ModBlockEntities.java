package com.sora.crossgamemod.block.entity;
import com.sora.crossgamemod.CrossGameMod;
import com.sora.crossgamemod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CrossGameMod.MODID);

    public static final RegistryObject<BlockEntityType<CrossingMachineEntity>> CROSSING_MACHINE =
            BLOCK_ENTITIES.register("crossing_machine", () ->
                    BlockEntityType.Builder.of(CrossingMachineEntity::new,
                            ModBlocks.CROSSING_MACHINE.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}