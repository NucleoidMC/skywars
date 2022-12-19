package us.potatoboy.skywars.custom.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// Taken from https://github.com/NucleoidMC/nucleoid-extras/blob/1.17/src/main/java/xyz/nucleoid/extras/lobby/block/LaunchPadBlock.java
public class LaunchPadBlock extends Block implements BlockEntityProvider, PolymerBlock {
    private final Block virtualBlock;

    public LaunchPadBlock(Settings settings, Block virtualBlock) {
        super(settings);
        this.virtualBlock = virtualBlock;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.virtualBlock;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity.isOnGround()) {
            var blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof LaunchPadBlockEntity launchPad) {
                entity.setVelocity(getVector(launchPad.getPitch(), entity.getYaw(0)).multiply(launchPad.getPower()));
                if (entity instanceof ServerPlayerEntity player) {
                    player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
                    player.playSound(SoundEvents.BLOCK_PISTON_EXTEND, SoundCategory.BLOCKS, 0.5f, 1);
                }
                super.onEntityCollision(state, world, pos, entity);
            }
        }
    }

    private static Vec3d getVector(float pitch, float yaw) {
        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);

        double horizontal = -Math.cos(pitchRad);
        return new Vec3d(
                Math.sin(yawRad) * horizontal,
                Math.sin(pitchRad),
                -Math.cos(yawRad) * horizontal
        );
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }
}