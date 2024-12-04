package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.item.DoubleTexturedItem;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class VacuumItem extends DoubleTexturedItem {
    public VacuumItem(Settings settings) {
        super("mob_vacuum", settings, Items.COAL);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            ItemStack newStack = player.getStackInHand(hand);

            if (!entity.isAlive()) return ActionResult.PASS;
            if (this.hasStoredEntity(newStack)) return ActionResult.PASS;
            this.setActivated(true);

            NbtCompound compound = saveEntity(entity);
            newStack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(compound));
            entity.stopRiding();
            entity.discard();
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (this.hasStoredEntity(context.getStack())) {
            World world = context.getWorld();
            if (world.isClient) {
                return ActionResult.PASS;
            } else {
                ItemStack itemInHand = context.getStack();
                BlockPos blockPos = context.getBlockPos();
                Direction direction = context.getSide();
                BlockState blockState = world.getBlockState(blockPos);

                BlockPos releasePos;
                if (blockState.getCollisionShape(world, blockPos).isEmpty())
                    releasePos = blockPos;
                else releasePos = blockPos.offset(direction);

                this.setActivated(false);

                NbtCompound tag = itemInHand.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
                if (EntityType.getEntityFromNbt(tag, world).map((entity) -> {
                    entity.setPos((double) releasePos.getX() + 0.5D, releasePos.getY(), (double) releasePos.getZ() + 0.5D);
                    entity.setVelocity(Vec3d.ZERO);
                    world.spawnEntity(entity);

                    return entity;
                }).isPresent()) {
                    world.emitGameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, releasePos);
                    itemInHand.remove(DataComponentTypes.ENTITY_DATA);
                }

                return ActionResult.SUCCESS;
            }
        } else {
            return ActionResult.PASS;
        }
    }

    public boolean hasStoredEntity(ItemStack itemStack) {
        return !itemStack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).isEmpty();
    }

    public static NbtCompound saveEntity(Entity entity) {
        NbtCompound compound = new NbtCompound();
        compound.putString("id", EntityType.getId(entity.getType()).toString());
        entity.saveNbt(compound);
        return compound;
    }
}
