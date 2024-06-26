package dev.greenhouseteam.effectapi.api.attachment;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import dev.greenhouseteam.effectapi.api.effect.ResourceEffect;
import dev.greenhouseteam.effectapi.impl.EffectAPI;
import dev.greenhouseteam.effectapi.impl.util.InternalResourceUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public record ResourcesAttachment(Map<ResourceLocation, ResourceEffect.ResourceHolder<Object>> resources) {
    public static final Codec<ResourcesAttachment> CODEC = new AttachmentCodec();

    @Nullable
    public <T> T getValue(ResourceLocation id) {
        return (T) Optional.ofNullable(resources.get(id)).map(ResourceEffect.ResourceHolder::getValue).orElse(null);
    }

    public List<ResourceEffect.ResourceHolder<Object>> getAllFromSource(ResourceLocation source) {
        return resources.values().stream().filter(holder -> holder.getSource().equals(source)).toList();
    }

    public <T> T setValue(ResourceLocation id, T value, ResourceLocation source) {
        if (!resources.containsKey(id)) {
            var holder = new ResourceEffect.ResourceHolder<>(InternalResourceUtil.getEffectFromId(id), source);
            holder.setValue(value);
            resources.put(id, holder);
        } else {
            resources.get(id).setValue(value);
        }
        return value;
    }

    public void removeValue(ResourceLocation id) {
        if (!resources.containsKey(id))
            return;
        resources.remove(id);
    }

    @Nullable
    public <T> ResourceEffect.ResourceHolder<T> getResourceHolder(ResourceLocation id) {
        return (ResourceEffect.ResourceHolder<T>) resources.get(id);
    }

    public static class AttachmentCodec implements Codec<ResourcesAttachment> {
        protected AttachmentCodec() {}

        @Override
        public <T> DataResult<Pair<ResourcesAttachment, T>> decode(DynamicOps<T> ops, T input) {
            DataResult<Stream<T>> stream = ops.getStream(input);
            if (stream.isError()) {
                return DataResult.error(() -> stream.error().get().message());
            }

            Map<ResourceLocation, ResourceEffect.ResourceHolder<Object>> finalMap = new HashMap<>();
            List<String> errors = new ArrayList<>();

            for (T entry : stream.getOrThrow().toList()) {
                DataResult<MapLike<T>> mapLike = ops.getMap(entry);
                if (mapLike.isError()) {
                    return DataResult.error(() -> stream.error().get().message());
                }

                DataResult<Pair<ResourceLocation, T>> id = ResourceLocation.CODEC.decode(ops, mapLike.getOrThrow().get("id"));
                if (id.isError()) {
                    errors.add("Failed to parse id in attachment. (Skipping). " + id.error().get().message());
                    continue;
                }

                ResourceEffect<Object> effect = InternalResourceUtil.getEffectFromId(id.getOrThrow().getFirst());

                if (effect == null) {
                    errors.add("Could not find resource effect with id '" + id.getOrThrow() + "'. (Skipping).");
                    continue;
                }

                var value = effect.getDefaultValue();
                if (mapLike.getOrThrow().get("value") != null) {
                    var newValue = effect.getResourceTypeCodec().decode(ops, mapLike.getOrThrow().get("value"));
                    if (newValue.isError()) {
                        errors.add("Failed to decode value to attachment. (Skipping). " + newValue.error().get().message());
                        continue;
                    }
                    value = newValue.getOrThrow().getFirst();
                }

                var newSource = ResourceLocation.CODEC.decode(ops, mapLike.getOrThrow().get("source"));
                if (newSource.isError()) {
                    errors.add("Failed to decode value to attachment. (Skipping). " + newSource.error().get().message());
                    continue;
                }

                ResourceEffect.ResourceHolder<Object> holder = new ResourceEffect.ResourceHolder<>(effect, newSource.getOrThrow().getFirst());
                holder.setValue(value);

                finalMap.put(id.getOrThrow().getFirst(), holder);
            }

            for (String error : errors) {
                EffectAPI.LOG.error(error);
            }

            return DataResult.success(Pair.of(new ResourcesAttachment(finalMap), input));
        }

        @Override
        public <T> DataResult<T> encode(ResourcesAttachment input, DynamicOps<T> ops, T prefix) {
            List<T> list = new ArrayList<>();

            for (Map.Entry<ResourceLocation, ResourceEffect.ResourceHolder<Object>> entry : input.resources.entrySet()) {
                Map<T, T> map = new HashMap<>();
                try {
                    map.put(ops.createString("id"), ResourceLocation.CODEC.encodeStart(ops, entry.getKey()).getOrThrow());
                    map.put(ops.createString("value"), entry.getValue().getEffect().getResourceTypeCodec().encodeStart(ops, entry.getValue().getValue()).getOrThrow());
                    map.put(ops.createString("source"), ResourceLocation.CODEC.encodeStart(ops, entry.getValue().getSource()).getOrThrow());
                } catch (Exception ex) {
                    EffectAPI.LOG.error("Failed to encode resource '{}' to attachment. (Skipping).", entry.getKey(), ex);
                }
                list.add(ops.createMap(map));
            }
            return DataResult.success(ops.createList(list.stream()));
        }
    }
}
