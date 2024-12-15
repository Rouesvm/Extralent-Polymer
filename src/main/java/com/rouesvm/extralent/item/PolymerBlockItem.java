package com.rouesvm.extralent.item;

import com.rouesvm.extralent.Extralent;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class PolymerBlockItem extends BlockItem implements PolymerItem, PolymerKeepModel {
    private final PolymerModelData model;

    public PolymerBlockItem(Settings settings, Block block, String name) {
        super(block, settings);
        this.model = PolymerResourcePackUtils.requestModel(Items.POISONOUS_POTATO,
                Identifier.of(Extralent.MOD_ID, "item/block/" + name));
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState state) {
        return SoundEvents.BLOCK_METAL_HIT;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getWorld() == null || context.getWorld().isClient) return ActionResult.FAIL;
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        if (player != null) {
            player.playSoundToPlayer(SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1, 0.5F * context.getWorld().getRandom().nextFloat() * 0.8F);
            player.swingHand(context.getHand(), true);
        }
        return super.useOnBlock(context);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }
}
