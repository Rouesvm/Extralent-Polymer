package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.registries.data.DataComponentRegistry;
import net.minecraft.advancement.criterion.PlayerInteractedWithEntityCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.List;

public class VacuumItem extends DoubleTexturedItem implements BasicEnergyItem {

    public VacuumItem(Settings settings) {
        super("mob_vacuum", settings, Items.COAL);
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return 2_500;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return 500;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }

    @Override
    public long getEnergyCost() {
        return 24;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        addEnergyTooltip(tooltip, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world != null && !world.isClient) {
            if (!(entity instanceof PlayerEntity player)) return;
            if (shouldPass(stack, player, false)) {
                spawnEntity(stack, entity.getBlockPos(), player, world);
                return;
            }
            if (!Activated.showVisual(stack)) return;

            deductEnergy(world, stack, 45);
        }
    }

    private void deductEnergy(World world, ItemStack stack, int intervalTicks) {
        long lastUpdateTime = stack.getOrDefault(DataComponentRegistry.LAST_UPDATE_TYPE, -1L);
        long currentTime = world.getTime();

        if (currentTime - lastUpdateTime >= intervalTicks) {
            long currentEnergy = getStoredEnergy(stack);

            if (currentEnergy >= getEnergyCost()) {
                setStoredEnergy(stack, currentEnergy - getEnergyCost());
                stack.set(DataComponentRegistry.LAST_UPDATE_TYPE, currentTime);
            }
        }
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            if (shouldPass(stack, player, true)) return ActionResult.PASS;

            ItemStack newStack = player.getStackInHand(hand);

            if (!entity.isAlive()) return ActionResult.PASS;

            if (entity instanceof PlayerEntity
                    || entity instanceof EnderDragonEntity
                    || entity instanceof WitherEntity
            ) return ActionResult.PASS;

            if (this.hasStoredEntity(newStack)) return ActionResult.PASS;

            setTexture(newStack, true);

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
        if (context.getWorld() == null || context.getWorld().isClient) return ActionResult.FAIL;

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

                spawnEntity(itemInHand, releasePos, context.getPlayer(), world);
                return ActionResult.SUCCESS;
            }
        } else return ActionResult.PASS;
    }

    public void spawnEntity(ItemStack stack, BlockPos pos, PlayerEntity player, World world) {
        NbtCompound tag = stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        if (tag.isEmpty()) return;
        if (EntityType.getEntityFromNbt(tag, world).map((entity) -> {
            entity.setPos((double) pos.getX() + 0.5D, pos.getY(), (double) pos.getZ() + 0.5D);
            entity.setVelocity(Vec3d.ZERO);
            world.spawnEntity(entity);

            return entity;
        }).isPresent()) {
            world.emitGameEvent(player, GameEvent.ENTITY_PLACE, pos);
            stack.remove(DataComponentTypes.ENTITY_DATA);
            setTexture(stack, false);
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

    @Override
    public void onLowEnergy(ItemStack stack, PlayerEntity player) {
        this.setTexture(stack, false);
    }
}
