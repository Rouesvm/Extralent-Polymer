package com.rouesvm.extralent.item.custom.info;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.item.custom.data.BasicData;
import com.rouesvm.extralent.visual.elements.InfoText;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

import static com.rouesvm.extralent.Extralent.ELEMENT_MANAGER;

public class InfoLogic {
    public static boolean canTick(@NotNull ItemStack stack, @NotNull Entity entity) {
        if (!Activated.showVisual(stack)) return false;

        BlockPos pos = InfoData.getBlockPos(stack);
        if (pos == null || !entity.getBlockPos().isWithinDistance(pos, 10)) {
            removeVisuals(stack);
            return false;
        }

        return true;
    }

    public static void showUIMessage(ServerWorld world, PlayerEntity player, ItemStack stack) {
        BlockPos pos = InfoData.getBlockPos(stack);
        if (pos == null) return;

        BlockEntity entity = world.getBlockEntity(pos);
        if (!(entity instanceof BasicMachineBlockEntity machine)) return;
        if (machine.infoOnClicked() == null) return;

        player.sendMessage(machine.infoOnClicked(InfoData.getContent(stack)), true);
    }

    public static boolean removeVisuals(@NotNull ItemStack stack) {
        UUID uuid = BasicData.getUuid(stack);
        ELEMENT_MANAGER.removeElement(uuid);

        if (Activated.showVisual(stack)) {
            Activated.setVisual(stack, false);
            return true;
        }

        return false;
    }

    public static void updateFloatingText(ServerWorld world, BlockPos pos, Direction face, BasicMachineBlockEntity machine, ItemStack stack) {
        UUID uuid = BasicData.getUuid(stack);
        Vec3d displayPos = pos.toCenterPos().offset(face, 1).add(0, 0.275, 0);

        ELEMENT_MANAGER.removeElement(uuid);
        ELEMENT_MANAGER.createElement(uuid, InfoText.createText(displayPos, machine, world));
    }

    public static boolean setContent(ItemStack stack, ServerWorld world) {
        InfoData.nextContent(stack);

        if (InfoData.getBlockPos(stack) != null) {
            var blockEntity = world.getBlockEntity(InfoData.getBlockPos(stack));
            if (!(blockEntity instanceof BasicMachineBlockEntity machine)) return false;
            if (Objects.equals(machine.infoOnClicked(InfoData.getContent(stack)), Text.empty())) {
                InfoData.nextContent(stack);
            }
        }

        return true;
    }
}
