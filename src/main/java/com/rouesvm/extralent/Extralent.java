package com.rouesvm.extralent;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.registries.block.BlockRegistry;
import com.rouesvm.extralent.registries.item.ItemRegistry;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Extralent implements ModInitializer {
	public static final String MOD_ID = "extralent";

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		ItemRegistry.initialize();
		ItemRegistry.initialize();

		BlockRegistry.initialize();
		BlockEntityRegistry.initialize();

		PolymerItemGroupUtils.registerPolymerItemGroup(of("extralent_group"), PolymerItemGroupUtils.builder()
				.icon(() -> new ItemStack(ItemRegistry.CONNECTOR))
				.displayName(Text.translatable("item.extralent.extralent_group"))
				.entries((displayContext, entries) -> {
					entries.add(ItemRegistry.CONNECTOR);
					entries.add(ItemRegistry.INFO);
					entries.add(BlockRegistry.GENERATOR);
					entries.add(BlockRegistry.TRANSMITTER);
					entries.add(BlockRegistry.TRANSPORTER);
					entries.add(BlockRegistry.QUARRY);
				}).build());
	}

	public static Identifier of(String name) {
		return Identifier.of(MOD_ID, name);
	}
}
