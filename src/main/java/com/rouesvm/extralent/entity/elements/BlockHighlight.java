package com.rouesvm.extralent.entity.elements;

import com.rouesvm.extralent.entity.InvisibleCube;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockHighlight extends ElementHolder {
    private final InvisibleCube markerElement;

    private BlockHighlight(BlockPos position) {
        this.markerElement = new InvisibleCube(position.toBottomCenterPos());
        this.markerElement.setGlowing(true);
        this.markerElement.setInvisible(true);
        this.addElement(markerElement);
    }

    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        return super.startWatching(player);
    }

    public void kill() {
        this.destroy();
    }

    public static BlockHighlight createHighlight(ServerWorld world, BlockPos position) {
        BlockHighlight model = new BlockHighlight(position);
        ChunkAttachment.of(model, world, position);
        return model;
    }
}
