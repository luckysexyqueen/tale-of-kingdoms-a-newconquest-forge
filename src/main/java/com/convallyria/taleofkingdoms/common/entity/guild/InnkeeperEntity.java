package com.convallyria.taleofkingdoms.common.entity.guild;

import com.convallyria.taleofkingdoms.TaleOfKingdoms;
import com.convallyria.taleofkingdoms.client.gui.entity.InnkeeperScreen;
import com.convallyria.taleofkingdoms.client.translation.Translations;
import com.convallyria.taleofkingdoms.common.entity.TOKEntity;
import com.convallyria.taleofkingdoms.client.schematic.ClientConquestInstance;
import com.convallyria.taleofkingdoms.common.world.ConquestInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class InnkeeperEntity extends TOKEntity {

    public InnkeeperEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 15.0F, 100F));
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (hand == Hand.OFF_HAND || !player.world.isClient()) return ActionResult.FAIL;
        ClientConquestInstance instance = (ClientConquestInstance) TaleOfKingdoms.getAPI().getConquestInstanceStorage().mostRecentInstance().get();
        if (!instance.hasContract()) {
            Translations.NEED_CONTRACT.send(player);
            return ActionResult.FAIL;
        }

        this.openScreen(player, instance);
        return ActionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    private void openScreen(PlayerEntity player, ConquestInstance instance) {
        InnkeeperScreen screen = new InnkeeperScreen(player, this, instance);
        MinecraftClient.getInstance().setScreen(screen);
    }

    @Override
    public boolean isStationary() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
