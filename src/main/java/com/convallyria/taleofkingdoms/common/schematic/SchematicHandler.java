package com.convallyria.taleofkingdoms.common.schematic;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.common.schematic.blocky.SchematicLoader;
import com.convallyria.taleofkingdoms.common.schematic.blocky.SchematicNotLoadedException;
import com.sk89q.worldedit.math.BlockVector3;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Handles schematics for TaleOfKingdoms.
 * Works on both SERVER and CLIENT.
 */
public abstract class SchematicHandler {

    /**
     * Pastes the selected schematic. Returns a {@link CompletableFuture} containing the {@link OperationInstance}
     * @param schematic schematic to paste
     * @param player the <b><i>server</i></b> player
     * @param position the {@link BlockVector3} position to paste at
     * @return {@link CompletableFuture} containing the {@link OperationInstance}
     */
    public abstract CompletableFuture<OperationInstance> pasteSchematic(Schematic schematic, ServerPlayerEntity player, BlockVector3 position);

    /**
     * Pastes the selected schematic. Returns a {@link CompletableFuture} containing the {@link OperationInstance}.
     * This defaults the position parameter to: <br>
     *     <b>x, y + 1, z</b>
     * @see #pasteSchematic(Schematic, ServerPlayerEntity, BlockVector3) 
     * @param schematic schematic to paste
     * @param player the <b><i>server</i></b> player
     * @return {@link CompletableFuture} containing the {@link OperationInstance}
     */
    @NotNull
    public CompletableFuture<OperationInstance> pasteSchematic(Schematic schematic, ServerPlayerEntity player) {
        BlockVector3 position = BlockVector3.at(player.getBlockPos().getX(), player.getBlockPos().getY() + 1, player.getBlockPos().getZ());
	    return pasteSchematic(schematic, player, position);
    }

    protected void pasteSchematic(Schematic schematic, ServerPlayerEntity player, BlockVector3 position, CompletableFuture<OperationInstance> cf) {
        TaleOfKingdoms.LOGGER.info("Loading schematic, please wait: " + schematic.toString());
        BlockPos blockPos = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        SchematicLoader schematicLoader = new SchematicLoader(TaleOfKingdoms.getAPI().get(), schematic.getFile());
        schematicLoader.loadSchematic();
        try {
            schematicLoader.pasteSchematic(blockPos, player, 0, SchematicLoader.Options.PLACE_ANYWHERE);
        } catch (SchematicNotLoadedException e) {
            e.printStackTrace();
        }
        /*World adaptedWorld = FabricAdapter.adapt(player.getServerWorld());
        ClipboardFormat format = ClipboardFormats.findByFile(schematic.getFile());
        try {
            Clipboard clipboard = format.getReader(new FileInputStream(schematic.getFile())).read();
            EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(adaptedWorld)
                    .maxBlocks(-1)
                    .build();

            ClipboardHolder clipboardHolder = new ClipboardHolder(clipboard);
            Operation operation = clipboardHolder.createPaste(editSession)
                    .to(position)
                    .ignoreAirBlocks(false)
                    .build();
            final UUID uuid = UUID.randomUUID();
            Operations.complete(operation);
            editSession.close();

            Region region = clipboard.getRegion();
            BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
            Vector3 realTo = position.toVector3().add(clipboardHolder.getTransform().apply(clipboardOffset.toVector3()));
            Vector3 max = realTo.add(clipboardHolder.getTransform().apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));
            RegionSelector selector = new CuboidRegionSelector(adaptedWorld, realTo.toBlockPoint(), max.toBlockPoint());

            BlockVector3 centerY = selector.getRegion().getCenter().toBlockPoint();
            TaleOfKingdoms.LOGGER.info(centerY); // Mainly debug, can be used to find the schematic in the world

            cf.complete(new OperationInstance(uuid, selector.getRegion()));
        } catch (WorldEditException | IOException e) {
            e.printStackTrace();
        }*/
    }
}
