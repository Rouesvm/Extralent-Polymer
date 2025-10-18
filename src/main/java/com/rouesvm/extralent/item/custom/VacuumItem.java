package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.item.DoubleTexturedItem;
import com.rouesvm.extralent.item.custom.data.Activated;
import com.rouesvm.extralent.registries.data.DataComponentRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class VacuumItem extends DoubleTexturedItem implements BasicEnergyItem {

    public VacuumItem(Settings settings) {
        super("mob_vacuum", settings, Items.COAL);
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return 2_000;
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
        return 25;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        tooltip.add(Text.translatable("general.info.stored_energy")
                .append(" ")
                .append(String.valueOf(getStoredEnergy(stack)))
                .setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (world != null && !world.isClient()) {
            if (!(entity instanceof PlayerEntity player)) return;
            if (!Activated.showVisual(stack)) return;
            if (shouldPass(stack, player, true)) {
                stack.remove(DataComponentRegistry.LAST_UPDATE_TYPE);
                spawnEntity(stack, entity.getBlockPos(), player, world);
                return;
            }
            deductEnergy(world, stack, 50);
        }
    }

    private void deductEnergy(World world, ItemStack stack, int intervalTicks) {
        long currentTime = world.getTime();
        long lastUpdateTime = stack.getOrDefault(DataComponentRegistry.LAST_UPDATE_TYPE, -1L);

        if (lastUpdateTime < 0) {
            stack.set(DataComponentRegistry.LAST_UPDATE_TYPE, currentTime);
            return;
        }

        long elapsedTicks = currentTime - lastUpdateTime;
        if (elapsedTicks >= intervalTicks) {
            long currentEnergy = getStoredEnergy(stack);

            float energyCostPerTick = (float) getEnergyCost() / 20;
            long energyCostOverall = (long) (energyCostPerTick * elapsedTicks);

            if (currentEnergy >= energyCostOverall) {
                setStoredEnergy(stack, Math.max(currentEnergy - energyCostOverall, 0));
                stack.set(DataComponentRegistry.LAST_UPDATE_TYPE, currentTime);
            } else setStoredEnergy(stack, 0);
        }
    }
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (player instanceof ServerPlayerEntity) {
            if (shouldPass(stack, player, true)) return ActionResult.PASS;
            stack.remove(DataComponentRegistry.LAST_UPDATE_TYPE);

            ItemStack newStack = player.getStackInHand(hand);

            if (!entity.isAlive()) return ActionResult.PASS;

            if (entity instanceof PlayerEntity
                    || entity instanceof EnderDragonEntity
                    || entity instanceof WitherEntity
            ) return ActionResult.PASS;

            if (this.hasStoredEntity(newStack)) return ActionResult.PASS;

            setTexture(newStack, true);

            NbtCompound compound = saveEntity(entity);
            newStack.set(DataComponentTypes.BUCKET_ENTITY_DATA, NbtComponent.of(compound));
            entity.stopRiding();
            entity.discard();

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld() == null || context.getWorld().isClient()) return ActionResult.FAIL;

        if (this.hasStoredEntity(context.getStack())) {
            World world = context.getWorld();
            if (world.isClient()) {
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
        NbtCompound tag = stack.getOrDefault(DataComponentTypes.BUCKET_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        if (tag.isEmpty()) return;
        if (EntityType.getEntityFromData(NbtReadView.create(ErrorReporter.EMPTY, world.getRegistryManager(), tag), world, SpawnReason.EVENT).map((entity) -> {
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
        var entity_data = itemStack.get(DataComponentTypes.BUCKET_ENTITY_DATA);
        if (entity_data != null)
            return entity_data.isEmpty();
        else return false;
    }

    public static NbtCompound saveEntity(Entity entity) {
        NbtWriteView compound = NbtWriteView.create(ErrorReporter.EMPTY, entity.getEntityWorld().getRegistryManager());
        compound.putString("id", EntityType.getId(entity.getType()).toString());
        entity.saveData(compound);
        return compound.getNbt();
    }

    @Override
    public void onLowEnergy(ItemStack stack, PlayerEntity player) {
        setTexture(stack, false);
    }
}
