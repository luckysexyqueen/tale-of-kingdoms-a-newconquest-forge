package com.convallyria.taleofkingdoms.mixin.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundSetStructureBlockPacket.class)
public class ClientUpdateStructureBlockUnlimit {

    @Shadow @Final @Mutable private BlockPos pos;
    @Shadow @Final @Mutable private StructureBlockEntity.UpdateType updateType;
    @Shadow @Final @Mutable private StructureMode mode;
    @Shadow @Final @Mutable private String name;
    @Shadow @Final @Mutable private BlockPos offset;
    @Shadow @Final @Mutable private Vec3i size;
    @Shadow @Final @Mutable private Mirror mirror;
    @Shadow @Final @Mutable private Rotation rotation;
    @Shadow @Final @Mutable private String data;
    @Shadow @Final @Mutable private boolean ignoreEntities;
    @Shadow @Final @Mutable private boolean showAir;
    @Shadow @Final @Mutable private boolean showBoundingBox;
    @Shadow @Final @Mutable private float integrity;
    @Shadow @Final @Mutable private long seed;

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"))
    private void reinit(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.updateType = friendlyByteBuf.readEnum(StructureBlockEntity.UpdateType.class);
        this.mode = friendlyByteBuf.readEnum(StructureMode.class);
        this.name = friendlyByteBuf.readUtf();
        this.offset = new BlockPos(Mth.clamp(friendlyByteBuf.readByte(), -512, 512), Mth.clamp(friendlyByteBuf.readByte(), -512, 512), Mth.clamp(friendlyByteBuf.readByte(), -512, 512));
        this.size = new Vec3i(Mth.clamp(friendlyByteBuf.readByte(), 0, 512), Mth.clamp(friendlyByteBuf.readByte(), 0, 512), Mth.clamp(friendlyByteBuf.readByte(), 0, 512));
        this.mirror = friendlyByteBuf.readEnum(Mirror.class);
        this.rotation = friendlyByteBuf.readEnum(Rotation.class);
        this.data = friendlyByteBuf.readUtf(128);
        this.integrity = Mth.clamp(friendlyByteBuf.readFloat(), 0.0F, 1.0F);
        this.seed = friendlyByteBuf.readVarLong();
        int k = friendlyByteBuf.readByte();
        this.ignoreEntities = (k & 1) != 0;
        this.showAir = (k & 2) != 0;
        this.showBoundingBox = (k & 4) != 0;
    }
}
