package com.rouesvm.extralent.registries.data;

import com.mojang.serialization.Codec;
import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class DataComponentRegistry {
    public static final ComponentType<Boolean> BOOLEAN_TYPE = register(
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build(),
            "state"
    );

    private static <T> ComponentType<T> register(ComponentType<T> type, String name) {
        var registry = Registry.register(Registries.DATA_COMPONENT_TYPE, Extralent.of(name), type);
        PolymerComponent.registerDataComponent(registry);
        return registry;
    }

    public static void initialize() {
    }
}
