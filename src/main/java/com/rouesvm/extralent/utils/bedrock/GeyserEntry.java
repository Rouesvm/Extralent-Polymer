package com.rouesvm.extralent.utils.bedrock;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.CustomBlockState;
import org.geysermc.geyser.api.block.custom.NonVanillaCustomBlockData;
import org.geysermc.geyser.api.block.custom.component.BoxComponent;
import org.geysermc.geyser.api.block.custom.component.CustomBlockComponents;
import org.geysermc.geyser.api.block.custom.component.GeometryComponent;
import org.geysermc.geyser.api.block.custom.component.MaterialInstance;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;
import org.geysermc.geyser.api.util.CreativeCategory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.rouesvm.extralent.Extralent.MOD_ID;

public class GeyserEntry implements EventRegistrar {
    public static Path PACKS_FOLDER;
    public static Path GEYSER_PACK;

    static GeyserApi geyser;

    public static void initialize() {
        loadResourcePack();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new GeyserEntry();
            geyser.eventBus().register(registrar, registrar);
        });
    }

    public static void loadResourcePack() {
        PACKS_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");
        GEYSER_PACK = PACKS_FOLDER.resolve(MOD_ID + ".zip");

        try {
            if (!GEYSER_PACK.toFile().exists()) {
                Files.createDirectories(PACKS_FOLDER);
                Path file = FabricLoader.getInstance().getModContainer(MOD_ID).flatMap(
                        modContainer -> modContainer.findPath("bedrock/" + MOD_ID + ".zip")).get();
                Files.copy(file, GEYSER_PACK);

                GEYSER_PACK = PACKS_FOLDER.resolve(MOD_ID + ".zip");
            }
        } catch (Exception e) {
            throw new RuntimeException("PACK doesn't exist!");
        }
    }

    @Subscribe
    public void onDefineResource(GeyserLoadResourcePacksEvent event) {
        if (GEYSER_PACK.toFile().exists()) event.resourcePacks().add(GEYSER_PACK);
    }

    @Subscribe
    public void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        for (Map.Entry<RegistryKey<Item>, Item> entry : Registries.ITEM.getEntrySet()) {
            var item = entry.getValue();
            if (item instanceof BedrockItem) {
                int id = Registries.ITEM.getRawId(item);
                Identifier identifier = entry.getKey().getValue();

                String translatedString = Text.translatable(item.getTranslationKey()).getString();

                NonVanillaCustomItemData customItemData = NonVanillaCustomItemData.builder()
                        .displayName(translatedString)
                        .name(translatedString)
                        .javaId(id)
                        .stackSize(item.getDefaultStack().getMaxCount())
                        .identifier(identifier.toString())
                        .translationString(item.getTranslationKey())
                        .allowOffhand(true)
                        .displayHandheld(true)
                        .icon(identifier.toString())
                        .creativeCategory(3)
                        .build();
                event.register(customItemData);
            }
        }
    }

    @Subscribe
    public void onGeyserDefineCustomBlockEvent(GeyserDefineCustomBlocksEvent event) {
        for (Map.Entry<RegistryKey<Block>, Block> entry : Registries.BLOCK.getEntrySet()) {
            var block = entry.getValue();
            if (block instanceof BedrockBlock) {
                Identifier identifier = entry.getKey().getValue();

                BlockState state = block.getDefaultState();

                CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                        .collisionBox(BoxComponent.emptyBox())
                        .selectionBox(BoxComponent.emptyBox())
                        .geometry(GeometryComponent.builder()
                                .identifier("geometry." + identifier.getNamespace() + "." + identifier.getPath())
                                .build())
                        .lightDampening(state.getOpacity())
                        .lightEmission(state.getLuminance())
                        .friction(block.getSlipperiness())
                        .build();

                NonVanillaCustomBlockData customBlockData = NonVanillaCustomBlockData.builder()
                        .name(identifier.getPath())
                        .namespace(identifier.getNamespace())
                        .components(customBlockComponents)
                        .creativeCategory(CreativeCategory.CONSTRUCTION)
                        .build();

                JavaBlockState javaBlockState = JavaBlockState.builder()
                        .javaId(Registries.BLOCK.getRawId(block))
                        .identifier(identifier.toString())
                        .build();

                event.registerOverride(javaBlockState, (CustomBlockState) customBlockData);
            }
        }
    }

    public static boolean isPlayerOnBedrock(ServerPlayerEntity player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUuid());
    }
}
