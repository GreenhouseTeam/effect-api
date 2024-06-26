package dev.greenhouseteam.effectapi.impl.util;

import dev.greenhouseteam.effectapi.api.effect.EffectAPIConditionalEffect;
import dev.greenhouseteam.effectapi.api.effect.EffectAPIEffect;
import dev.greenhouseteam.effectapi.api.registry.EffectAPILootContextParams;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class InternalEffectUtil {
    public static void executeOnAllEffects(DataComponentMap map, Consumer<EffectAPIEffect> consumer) {
        for (var entry : map)
            if (entry.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                list.forEach(effect -> consumer.accept((EffectAPIEffect)effect));
    }

    public static <T extends EffectAPIEffect> T castConditional(EffectAPIEffect effect) {
        return (T) ((EffectAPIConditionalEffect)effect).effect();
    }

    public static DataComponentMap generateActiveEffects(LootContext context, LootContextParamSet paramSet,
                                                         DataComponentMap map,
                                                         Map<EffectAPIEffect, ResourceLocation> sources) {
        Map<DataComponentType<?>, List<EffectAPIEffect>> newMap = new Reference2ObjectArrayMap<>();

        for (TypedDataComponent<?> component : map) {
            if (component.value() instanceof List<?> list && list.getFirst() instanceof EffectAPIEffect)
                for (EffectAPIEffect effect : ((List<EffectAPIEffect>) list)) {
                    LootParams.Builder paramBuilder = LootContextUtil.copyIntoParamBuilder(context);
                    if (paramSet.isAllowed(EffectAPILootContextParams.SOURCE) && !context.hasParam(EffectAPILootContextParams.SOURCE))
                        paramBuilder.withOptionalParameter(EffectAPILootContextParams.SOURCE, sources.get(effect));
                    LootContext context1 = new LootContext.Builder(paramBuilder.create(paramSet)).create(Optional.empty());
                    if (effect.paramSet() == paramSet && effect.isActive(context1))
                        newMap.computeIfAbsent(component.type(), type -> new ArrayList<>()).add(effect);
                }
        }

        DataComponentMap.Builder builder = DataComponentMap.builder();
        for (var entry : newMap.entrySet())
            builder.set((DataComponentType<? super List<EffectAPIEffect>>) entry.getKey(), entry.getValue());
        return builder.build();
    }

    public static boolean hasUpdatedActives(LootContext context, LootContextParamSet paramSet,
                                            DataComponentMap newMap, DataComponentMap oldMap,
                                            Map<EffectAPIEffect, ResourceLocation> sources) {
        List<?> oldValues = oldMap.stream().flatMap(component -> ((List<?>)component.value()).stream()).toList();
        List<?> newValues = newMap.stream().flatMap(component -> ((List<?>)component.value()).stream()).toList();

        if (oldValues.equals(newValues))
            return false;

        newValues.stream().filter(object -> !oldValues.contains(object)).forEach(value -> {
            if (value instanceof EffectAPIEffect effect)
                if (effect.paramSet() == paramSet && sources.containsKey(effect)) {
                    LootParams.Builder paramBuilder = LootContextUtil.copyIntoParamBuilder(context);
                    if (paramSet.isAllowed(EffectAPILootContextParams.SOURCE) && !context.hasParam(EffectAPILootContextParams.SOURCE))
                        paramBuilder.withOptionalParameter(EffectAPILootContextParams.SOURCE, sources.get(effect));
                    LootContext context1 = new LootContext.Builder(paramBuilder.create(paramSet)).create(Optional.empty());
                    effect.onAdded(context1);
                }
        });
        oldValues.stream().filter(object -> !newValues.contains(object)).forEach(value -> {
            if (value instanceof EffectAPIEffect effect)
                if (effect.paramSet() == paramSet) {
                    LootParams.Builder paramBuilder = LootContextUtil.copyIntoParamBuilder(context);
                    if (paramSet.isAllowed(EffectAPILootContextParams.SOURCE) && !context.hasParam(EffectAPILootContextParams.SOURCE))
                        paramBuilder.withOptionalParameter(EffectAPILootContextParams.SOURCE, sources.get(effect));
                    LootContext context1 = new LootContext.Builder(paramBuilder.create(paramSet)).create(Optional.empty());
                    effect.onRemoved(context1);
                }
        });

        return true;
    }
}
