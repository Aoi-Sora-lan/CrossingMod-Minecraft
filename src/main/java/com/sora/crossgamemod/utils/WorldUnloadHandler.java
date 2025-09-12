package com.sora.crossgamemod.utils;

import com.sora.crossgamemod.CrossGameMod;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CrossGameMod.MODID)
public class WorldUnloadHandler {
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event){
        UdpSystem.get_instance().init();
    }
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        UdpSystem.get_instance().close();
    }
}
