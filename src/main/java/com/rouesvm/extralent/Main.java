package com.rouesvm.extralent;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.registries.block.BlockRegistry;
import com.rouesvm.extralent.registries.item.ItemRegistry;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
	public static final String MOD_ID = "extralent";

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		ItemRegistry.initialize();

		BlockRegistry.initialize();
		BlockEntityRegistry.initialize();
	}

	public static Identifier of(String name) {
		return Identifier.of(MOD_ID, name);
	}
}
