package net.islandearth.taleofkingdoms.common.listener;

import com.google.gson.Gson;
import com.sk89q.worldedit.math.BlockVector3;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.islandearth.taleofkingdoms.TaleOfKingdoms;
import net.islandearth.taleofkingdoms.common.entity.EntityTypes;
import net.islandearth.taleofkingdoms.common.entity.TOKEntity;
import net.islandearth.taleofkingdoms.common.event.GameInstanceCallback;
import net.islandearth.taleofkingdoms.common.event.PlayerJoinCallback;
import net.islandearth.taleofkingdoms.common.event.tok.KingdomStartCallback;
import net.islandearth.taleofkingdoms.common.schematic.Schematic;
import net.islandearth.taleofkingdoms.common.world.ServerConquestInstance;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.Tag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;

@Environment(EnvType.SERVER)
public class GameInstanceListener extends Listener {

    public GameInstanceListener() {
        GameInstanceCallback.EVENT.register(gameInstance -> {
            TaleOfKingdoms.getAPI().ifPresent(api -> api.setServer(gameInstance));
        });

        PlayerJoinCallback.EVENT.register((connection, player) -> {
            TaleOfKingdoms.getAPI().ifPresent(api -> {
                api.executeOnDedicatedServer(() -> {
                    MinecraftDedicatedServer server = api.getServer().get();
                    File file = new File(api.getDataFolder() + "worlds/" + server.getLevelName() + ".conquestworld");
                    int topY = server.getOverworld().getTopY(Heightmap.Type.WORLD_SURFACE, 0, 0);
                    api.getSchematicHandler().pasteSchematic(Schematic.GUILD_CASTLE, player, BlockVector3.at(0, topY, 0)).thenAccept(oi -> {
                        BlockVector3 max = oi.getRegion().getMaximumPoint();
                        BlockVector3 min = oi.getRegion().getMinimumPoint();
                        BlockPos start = new BlockPos(max.getBlockX(), max.getBlockY(), max.getBlockZ());
                        BlockPos end = new BlockPos(min.getBlockX(), min.getBlockY(), min.getBlockZ());
                        ServerConquestInstance instance = new ServerConquestInstance(server.getLevelName(), server.getName(), start, end);
                        instance.setBankerCoins(player.getUuid(), 0);
                        instance.setCoins(player.getUuid(), 0);
                        instance.setFarmerLastBread(player.getUuid(), 0);
                        instance.setHasContract(player.getUuid(), false);
                        instance.setWorthiness(player.getUuid(), 0);
                        try (Writer writer = new FileWriter(file)) {
                            Gson gson = TaleOfKingdoms.getAPI().get().getMod().getGson();
                            gson.toJson(instance, writer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            api.getConquestInstanceStorage().addConquest(server.getLevelName(), instance, true);
                            TaleOfKingdoms.LOGGER.info("Summoning citizens of the realm...");
                            int topBlockX = (Math.max(max.getBlockX(), min.getBlockX()));
                            int bottomBlockX = (Math.min(max.getBlockX(), min.getBlockX()));

                            int topBlockY = (Math.max(max.getBlockY(), min.getBlockY()));
                            int bottomBlockY = (Math.min(max.getBlockY(), min.getBlockY()));

                            int topBlockZ = (Math.max(max.getBlockZ(), min.getBlockZ()));
                            int bottomBlockZ = (Math.min(max.getBlockZ(), min.getBlockZ()));

                            for (int x = bottomBlockX; x <= topBlockX; x++) {
                                for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                                    for (int y = bottomBlockY; y <= topBlockY; y++) {
                                        BlockPos blockPos = new BlockPos(x, y, z);
                                        BlockEntity tileEntity = server.getOverworld().getChunk(blockPos).getBlockEntity(blockPos);
                                        if (tileEntity instanceof SignBlockEntity) {
                                            SignBlockEntity signTileEntity = (SignBlockEntity) tileEntity;
                                            Tag line1 = signTileEntity.toInitialChunkDataTag().get("Text1");
                                            if (line1 != null && line1.toText().getString().equals("'{\"text\":\"[Spawn]\"}'")) {
                                                Tag line2 = signTileEntity.toInitialChunkDataTag().get("Text2");
                                                String entityName = line2.toText().getString().replace("'{\"text\":\"", "").replace("\"}'", ""); // Doesn't seem to be a way to get the plain string...
                                                Class<? extends TOKEntity> entity = (Class<? extends TOKEntity>) Class.forName("net.islandearth.taleofkingdoms.common.entity.guild." + entityName + "Entity");
                                                Constructor constructor = entity.getConstructor(EntityType.class, World.class);
                                                EntityType type = (EntityType) EntityTypes.class.getField(entityName.toUpperCase()).get(EntityTypes.class);
                                                TOKEntity toSpawn = (TOKEntity) constructor.newInstance(type, player.getEntityWorld());
                                                toSpawn.setPos(x + 0.5, y, z + 0.5);
                                                server.getOverworld().spawnEntity(toSpawn);
                                                server.getOverworld().breakBlock(blockPos, false);
                                                toSpawn.refreshPositionAfterTeleport(x + 0.5, y, z + 0.5);
                                                TaleOfKingdoms.LOGGER.info("Spawned entity " + entityName + " " + toSpawn.toString() + " " + toSpawn.getX() + "," + toSpawn.getY() + "," + toSpawn.getZ());
                                            }
                                        }
                                    }
                                }
                            }
                            TaleOfKingdoms.LOGGER.info("COMPLETE");
                            KingdomStartCallback.EVENT.invoker().kingdomStart(player, instance); // Call kingdom start event
                            TaleOfKingdoms.LOGGER.info("SENT KINGDOM EVENT");
                            // We'll get to this later
                            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                            passedData.writeString(instance.getName());
                            passedData.writeString(instance.getWorld());
                            passedData.writeInt(instance.getBankerCoins(player.getUuid()));
                            passedData.writeInt(instance.getCoins(player.getUuid()));
                            passedData.writeInt(instance.getWorthiness(player.getUuid()));
                            passedData.writeLong(instance.getFarmerLastBread(player.getUuid()));
                            passedData.writeBoolean(instance.hasContract(player.getUuid()));
                            passedData.writeBoolean(instance.isLoaded());
                            passedData.writeBlockPos(instance.getStart());
                            passedData.writeBlockPos(instance.getEnd());
                            // Then we'll send the packet to all the players
                            TaleOfKingdoms.LOGGER.info("SENDING PACKET");
                            System.out.println(player + " " + TaleOfKingdoms.PLAY_INSTANCE_PACKET_ID + " " + passedData);
                            connection.send(new CustomPayloadS2CPacket(TaleOfKingdoms.PLAY_INSTANCE_PACKET_ID, passedData));
                            //ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, TaleOfKingdoms.PLAY_INSTANCE_PACKET_ID, passedData, fl1);
                            // This will work in both multiplayer and singleplayer!
                            TaleOfKingdoms.LOGGER.info("SENDING PACKET 2");
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                        }
                    });
                });
            });
        });
    }
}