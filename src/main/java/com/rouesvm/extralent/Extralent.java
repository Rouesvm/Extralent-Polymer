package com.rouesvm.extralent;

import com.rouesvm.extralent.registries.block.BlockEntityRegistry;
import com.rouesvm.extralent.registries.block.BlockRegistry;
import com.rouesvm.extralent.registries.data.DataComponentRegistry;
import com.rouesvm.extralent.registries.item.ItemRegistry;
import com.rouesvm.extralent.visual.ElementManager;
import com.rouesvm.extralent.visual.HighlightManager;
import com.rouesvm.extralent.visual.elements.BlockHighlights;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Extralent implements ModInitializer {
	public static final String MOD_ID = "extralent";

	public static final HighlightManager HIGHLIGHT_MANAGER = new HighlightManager();
	public static final ElementManager ELEMENT_MANAGER = new ElementManager();

	@Override
	public void onInitialize() {

        Runtime.getRuntime().addShutdownHook(new Thread(BlockHighlights::shutdownThread));

        PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		DataComponentRegistry.initialize();

		ItemRegistry.initialize();

		BlockRegistry.initialize();
		BlockEntityRegistry.initialize();

		PolymerItemGroupUtils.registerPolymerItemGroup(of("extralent_group"), PolymerItemGroupUtils.builder()
				.icon(() -> new ItemStack(ItemRegistry.CONNECTOR))
				.displayName(Text.translatable("item.extralent.extralent_group"))
				.entries((displayContext, entries) -> {
                    entries.add(ItemRegistry.PORTABLE_DRILL);

                    entries.add(ItemRegistry.INFO);
					entries.add(ItemRegistry.CONNECTOR);
					entries.add(ItemRegistry.VACUUM);
					entries.add(ItemRegistry.FILTER);

					entries.add(BlockRegistry.ELECTRIC_FURNACE);
					entries.add(BlockRegistry.GENERATOR);
					entries.add(BlockRegistry.HARVESTER);
					entries.add(BlockRegistry.QUARRY);

					entries.add(BlockRegistry.TELEPORTER);
					entries.add(BlockRegistry.TRANSMITTER);
					entries.add(BlockRegistry.TRANSPORTER);

                    entries.add(ItemRegistry.DRILL_HEAD);
                    entries.add(ItemRegistry.MACHINE_BASE);
					entries.add(ItemRegistry.COPPER_ROD);
					entries.add(ItemRegistry.ANTENNA);

					entries.add(ItemRegistry.JAR);
					entries.add(ItemRegistry.APPLE_JAM);
					entries.add(ItemRegistry.APPLE_JAM_SANDWICH);
				}).build());
	}

	public static Identifier of(String name) {
		return Identifier.of(MOD_ID, name);
	}
}
