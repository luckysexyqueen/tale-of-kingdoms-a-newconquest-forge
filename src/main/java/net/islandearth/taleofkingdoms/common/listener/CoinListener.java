package net.islandearth.taleofkingdoms.common.listener;

import net.islandearth.taleofkingdoms.TaleOfKingdoms;
import net.islandearth.taleofkingdoms.common.entity.generic.HunterEntity;
import net.islandearth.taleofkingdoms.common.event.EntityDeathCallback;
import net.islandearth.taleofkingdoms.common.event.EntityPickupItemCallback;
import net.islandearth.taleofkingdoms.common.item.ItemHelper;
import net.islandearth.taleofkingdoms.common.item.ItemRegistry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CoinListener extends Listener {

    public CoinListener() {
        EntityDeathCallback.EVENT.register((source, entity) -> {
            if (source.getSource() instanceof PlayerEntity || source.getSource() instanceof HunterEntity) {
                ItemHelper.dropCoins(entity);
                TaleOfKingdoms.getAPI().get().getConquestInstanceStorage().mostRecentInstance().ifPresent(instance -> {
                    instance.setWorthiness(instance.getWorthiness() + 1);
                });
            }
        });

        EntityPickupItemCallback.EVENT.register((player, item) -> {
            if (item.getItem().equals(ItemRegistry.ITEMS.get(ItemRegistry.TOKItem.COIN))) {
                Random random = ThreadLocalRandom.current();
                TaleOfKingdoms.getAPI().get()
                        .getConquestInstanceStorage()
                        .mostRecentInstance()
                        .get()
                        .addCoins(random.nextInt(50));
                player.inventory.remove(predicate -> predicate.getItem().equals(item.getItem()), -1, player.inventory);
            }
        });
    }
}
