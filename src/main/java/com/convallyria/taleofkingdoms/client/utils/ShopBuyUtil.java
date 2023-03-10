package com.convallyria.taleofkingdoms.client.utils;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.TaleOfKingdomsAPI;
import com.convallyria.taleofkingdoms.common.shop.ShopItem;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@Environment(EnvType.CLIENT)
public class ShopBuyUtil {

    public static void buyItem(ConquestInstance instance, PlayerEntity player, ShopItem shopItem, int count) {
        if (shopItem.canBuy(instance, player, count)) {
            final TaleOfKingdomsAPI api = TaleOfKingdoms.getAPI();
            api.executeOnMain(() -> {
                MinecraftServer server = MinecraftClient.getInstance().getServer();
                if (server == null) {
                    api.getClientHandler(TaleOfKingdoms.BUY_ITEM_PACKET_ID)
                            .handleOutgoingPacket(TaleOfKingdoms.BUY_ITEM_PACKET_ID,
                                    player, shopItem.getName(), count);
                    return;
                }

                ServerPlayerEntity serverPlayerEntity = server.getPlayerManager().getPlayer(player.getUuid());
                if (serverPlayerEntity != null) {
                    serverPlayerEntity.getInventory().insertStack(new ItemStack(shopItem.getItem(), count));
                    int cost = shopItem.getCost() * count;
                    instance.setCoins(instance.getCoins() - cost);
                }
            });
        }
    }
}
