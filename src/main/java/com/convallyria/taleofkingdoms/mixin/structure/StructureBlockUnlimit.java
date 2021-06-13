package com.convallyria.taleofkingdoms.mixin.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Mth;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Mixin(StructureBlockEntity.class)
public class StructureBlockUnlimit {

    @Shadow private ResourceLocation structureName;
    @Shadow private String author;
    @Shadow private String metaData;
    @Shadow private BlockPos structurePos;
    @Shadow private Vec3i structureSize;
    @Shadow private Mirror mirror;
    @Shadow private Rotation rotation;
    @Shadow private StructureMode mode;
    @Shadow private boolean ignoreEntities;
    @Shadow private boolean powered;
    @Shadow private boolean showAir;
    @Shadow private boolean showBoundingBox;
    @Shadow private float integrity;
    @Shadow private long seed;

    /**
     * @reason Increases structure block max size to 512
     * @author SamB440/Cotander
     */
    @Overwrite
    public void load(CompoundTag compoundTag) {
        StructureBlockEntity entity = (StructureBlockEntity) (Object) this;
        StructureBlockAccessor accessor = (StructureBlockAccessor) entity;
        // super.readNbt(nbtCompound);
        entity.setStructureName(compoundTag.getString("name"));
        this.author = compoundTag.getString("author");
        this.metaData = compoundTag.getString("metadata");
        int i = Mth.clamp(compoundTag.getInt("posX"), -512, 512);
        int j = Mth.clamp(compoundTag.getInt("posY"), -512, 512);
        int k = Mth.clamp(compoundTag.getInt("posZ"), -512, 512);
        this.structurePos = new BlockPos(i, j, k);
        int l = Mth.clamp(compoundTag.getInt("sizeX"), 0, 512);
        int m = Mth.clamp(compoundTag.getInt("sizeY"), 0, 512);
        int n = Mth.clamp(compoundTag.getInt("sizeZ"), 0, 512);
        this.structureSize = new Vec3i(l, m, n);

        try {
            this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
        } catch (IllegalArgumentException var11) {
            this.rotation = Rotation.NONE;
        }

        try {
            this.mirror = Mirror.valueOf(compoundTag.getString("mirror"));
        } catch (IllegalArgumentException var10) {
            this.mirror = Mirror.NONE;
        }

        try {
            this.mode = StructureMode.valueOf(compoundTag.getString("mode"));
        } catch (IllegalArgumentException var9) {
            this.mode = StructureMode.DATA;
        }

        this.ignoreEntities = compoundTag.getBoolean("ignoreEntities");
        this.powered = compoundTag.getBoolean("powered");
        this.showAir = compoundTag.getBoolean("showair");
        this.showBoundingBox = compoundTag.getBoolean("showboundingbox");
        if (compoundTag.contains("integrity")) {
            this.integrity = compoundTag.getFloat("integrity");
        } else {
            this.integrity = 1.0F;
        }

        this.seed = compoundTag.getLong("seed");
        accessor.updateBlockState();
    }
    
    /**
     * @reason Increases structure block detection size up to 255
     * @author SamB440/Cotander
     */
    @Overwrite
    public boolean detectSize() {
        StructureBlockEntity entity = (StructureBlockEntity) (Object) this;
        StructureBlockAccessor accessor = (StructureBlockAccessor) entity;
        if (this.mode != StructureMode.SAVE) {
            return false;
        } else {
            BlockPos blockPos = entity.getBlockPos();
            BlockPos blockPos2 = new BlockPos(blockPos.getX() - 80, entity.getLevel().getMinBuildHeight(), blockPos.getZ() - 80);
            BlockPos blockPos3 = new BlockPos(blockPos.getX() + 80, entity.getLevel().getMaxBuildHeight() - 1, blockPos.getZ() + 80);
            Stream<BlockPos> stream = accessor.getRelatedCorners(blockPos2, blockPos3);
            return calculateEnclosingBoundingBox(blockPos, stream).filter((boundingBox) -> {
                int i = boundingBox.maxX() - boundingBox.minX();
                int j = boundingBox.maxY() - boundingBox.minY();
                int k = boundingBox.maxZ() - boundingBox.minZ();
                if (i > 1 && j > 1 && k > 1) {
                    this.structurePos = new BlockPos(boundingBox.minX() - blockPos.getX() + 1, boundingBox.minY() - blockPos.getY() + 1, boundingBox.minZ() - blockPos.getZ() + 1);
                    this.structureSize = new Vec3i(i - 1, j - 1, k - 1);
                    this.setChanged();
                    BlockState blockState = this.level.getBlockState(blockPos);
                    this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                    return true;
                } else {
                    return false;
                }
            }).isPresent();
        }
    }

    /**
     * @reason Provides access to this method in Java 16.
     * @author SamB440
     */
    @Overwrite
    private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos blockPos, Stream<BlockPos> stream) {
        Iterator<BlockPos> iterator = stream.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BlockPos blockPos2 = iterator.next();
            BoundingBox boundingBox = new BoundingBox(blockPos2);
            if (iterator.hasNext()) {
                Objects.requireNonNull(boundingBox);
                iterator.forEachRemaining(boundingBox::encapsulate);
            } else {
                boundingBox.encapsulate(blockPos);
            }

            return Optional.of(boundingBox);
        }
    }
}
