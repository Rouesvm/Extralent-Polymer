package com.rouesvm.extralent.entity;

import eu.pb4.polymer.virtualentity.api.elements.GenericEntityElement;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.Vec3d;

public class InvisibleCube extends GenericEntityElement {

    public InvisibleCube(Vec3d pos) {
        this.setOverridePos(pos);
        this.dataTracker.set(EntityTrackedData.SILENT, true);
        this.dataTracker.set(EntityTrackedData.NO_GRAVITY, true);
        this.dataTracker.set(EntityTrackedData.FLAGS, (byte) ((1 << EntityTrackedData.INVISIBLE_FLAG_INDEX)));
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return EntityType.SHULKER;
    }
}
