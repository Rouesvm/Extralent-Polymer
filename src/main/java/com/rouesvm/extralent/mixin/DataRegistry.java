package com.rouesvm.extralent.mixin;

import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.impl.EnergyImpl;

@Mixin(EnergyImpl.class)
public class DataRegistry {
    @Shadow @Final public static ComponentType<Long> ENERGY_COMPONENT;

    @Inject(method = "init", at = @At("HEAD"), remap = false)
    private static void init(CallbackInfo ci) {
        PolymerComponent.registerDataComponent(ENERGY_COMPONENT);
    }
}
