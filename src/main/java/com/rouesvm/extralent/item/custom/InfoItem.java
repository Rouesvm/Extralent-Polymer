package com.rouesvm.extralent.item.custom;

import com.rouesvm.extralent.block.entity.BasicMachineBlockEntity;
import com.rouesvm.extralent.entity.elements.InfoText;
import com.rouesvm.extralent.item.BasicPolymerItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class InfoItem extends BasicPolymerItem {
    public InfoText floatingText;

    public InfoItem(Settings settings) {
        super("viewer", settings, Items.COAL);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient && player != null) {
            if (this.floatingText != null && player.isSneaking()) {
                this.floatingText.setDestroy(true);
                return ActionResult.SUCCESS;
            }
        }

        assert player != null;
        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient && context.getPlayer() != null) {
            ServerWorld world = (ServerWorld) context.getWorld();

            var blockEntityResult = world.getBlockEntity(context.getBlockPos());
            if (blockEntityResult instanceof BasicMachineBlockEntity basicPoweredEntity) {
                if (this.floatingText != null) this.floatingText.setDestroy(true);
                Direction direction = context.getSide();
                Vec3d displayPos = context.getBlockPos().toCenterPos().offset(direction, 1)
                        .add(new Vec3d(0, 0.275, 0));

                if (basicPoweredEntity.infoOnClicked() == null)
                    return ActionResult.PASS;

                this.floatingText = InfoText.createText(displayPos, basicPoweredEntity, world);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}
