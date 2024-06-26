package dev.greenhouseteam.effectapi.api.effect;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public interface EffectAPIEffect {
    default void onAdded(LootContext context) {}

    default void onRemoved(LootContext context) {}

    default void onRefreshed(LootContext context) {
        onRemoved(context);
    }

    default boolean isActive(LootContext context) {
        return true;
    }

    DataComponentType<?> type();

    LootContextParamSet paramSet();
}
