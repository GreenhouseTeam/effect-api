package dev.greenhouseteam.effectapi.impl;

import dev.greenhouseteam.effectapi.api.EffectAPIResourceTypes;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.registry.EffectAPIAttachments;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;

public class EffectAPIBaseFabric implements ModInitializer {
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        EffectAPIResourceTypes.registerAll(Registry::register);
        EffectAPIAttachments.init();

        ServerLifecycleEvents.SERVER_STARTED.register(EffectAPIBaseFabric::setServer);
        ServerLifecycleEvents.SERVER_STOPPED.register(server1 -> {
            ResourceEffect.ResourceEffectCodec.clearLoadedEffects();
            setServer(null);
        });
    }

    public static void setServer(MinecraftServer server) {
        EffectAPIBaseFabric.server = server;
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
