package com.sora.crossgamemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = CrossGameMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.IntValue CROSS_LOCAL_PORT = BUILDER
            .comment("Local port")
            .defineInRange("crossLocalPort", 12002, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue CROSS_SERVER_PORT = BUILDER
            .comment("Server port")
            .defineInRange("crossServerPort", 12000, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> CROSS_SERVER_IP = BUILDER
            .comment("Server ip")
            .define("crossServerIp", "127.0.0.1");

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int crossLocalPort;
    public static int crossServerPort;
    public static String crossServerIp;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        crossLocalPort = CROSS_LOCAL_PORT.get();
        crossServerPort = CROSS_SERVER_PORT.get();
        crossServerIp = CROSS_SERVER_IP.get();
    }
}
