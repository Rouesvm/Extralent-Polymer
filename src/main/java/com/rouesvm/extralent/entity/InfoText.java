package com.rouesvm.extralent.entity;

import com.rouesvm.extralent.block.entity.BasicPoweredEntity;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public class InfoText extends ElementHolder {
    private int timer;

    private final BasicPoweredEntity basicPoweredEntity;
    private final TextDisplayElement display;
    private boolean destroy;

    private InfoText(BasicPoweredEntity basicPoweredEntity, Vec3d pos) {
        this.timer = 200;

        this.basicPoweredEntity = basicPoweredEntity;
        this.display = new TextDisplayElement(basicPoweredEntity.infoOnClicked());
        this.display.setViewRange(0.2f);
        this.display.setShadow(false);
        this.display.setBackground(0);
        this.display.setBrightness(new Brightness(15, 15));
        this.display.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        this.display.setScale(new Vector3f(0.5F));
        this.display.setOverridePos(pos);
        this.display.setTeleportDuration(2);
        this.addElement(display);
    }

    @Override
    protected void onTick() {
        super.onTick();
        if (this.destroy) {
            this.destroy = false;
            this.destroy();
        }

        if (this.timer-- == 0) {
            this.destroy = true;
        } else if (this.timer == 5) {
            this.display.setScale(new Vector3f(0));
            this.display.setInterpolationDuration(5);
            this.display.startInterpolation();
        }

        this.display.setText(basicPoweredEntity.infoOnClicked());
    }

    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        return super.startWatching(player);
    }

    public static InfoText createText(Vec3d pos, BasicPoweredEntity basicPoweredEntity, ServerWorld world) {
        var model = new InfoText(basicPoweredEntity, pos);
        ChunkAttachment.ofTicking(model, world, pos);
        return model;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }
}