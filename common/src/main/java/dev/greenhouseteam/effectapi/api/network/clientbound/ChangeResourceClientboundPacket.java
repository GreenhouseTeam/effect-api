package dev.greenhouseteam.effectapi.api.network.clientbound;

import com.mojang.serialization.Codec;
import dev.greenhouseteam.effectapi.api.effect.Effect;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class ChangeResourceClientboundPacket<T> implements CustomPacketPayload {
    public static final ResourceLocation ID = EffectAPI.asResource("change_resource");
    public static final Type<ChangeResourceClientboundPacket<?>> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChangeResourceClientboundPacket<?>> STREAM_CODEC = StreamCodec.of(ChangeResourceClientboundPacket::write, ChangeResourceClientboundPacket::new);

    private final int entityId;
    private final ResourceEffect<T> resourceEffect;
    private final Optional<T> value;

    public ChangeResourceClientboundPacket(int entityId, ResourceEffect<T> resourceEffect, Optional<T> value) {
        this.entityId = entityId;
        this.resourceEffect = resourceEffect;
        this.value = value;
    }

    public ChangeResourceClientboundPacket(RegistryFriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.resourceEffect = ResourceEffect.getEffectFromId(buf.readResourceLocation());
        if (buf.readBoolean())
            this.value = Optional.of(resourceEffect.getResourceTypeCodec().fieldOf("value").codec().decode(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), buf.readNbt()).getOrThrow().getFirst());
        else
            this.value = Optional.empty();
    }

    public static void write(RegistryFriendlyByteBuf buf, ChangeResourceClientboundPacket<?> packet) {
        buf.writeInt(packet.entityId);
        buf.writeResourceLocation(packet.resourceEffect.getId());
        buf.writeBoolean(packet.value.isPresent());
        if (packet.value.isPresent())
            buf.writeNbt(((Codec<Object>)packet.resourceEffect.getResourceTypeCodec()).fieldOf("value").codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, buf.registryAccess()), packet.value.get()).getOrThrow());
    }

    public void handle() {
        Minecraft.getInstance().execute(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (value.isEmpty())
                EffectAPI.getHelper().removeResource(entity, resourceEffect.getId());
            else
                EffectAPI.getHelper().setResource(entity, resourceEffect.getId(), value.get());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
