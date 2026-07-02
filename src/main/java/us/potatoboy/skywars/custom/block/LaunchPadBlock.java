package us.potatoboy.skywars.custom.block;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// Taken from https://github.com/NucleoidMC/nucleoid-extras/blob/1.17/src/main/java/xyz/nucleoid/extras/lobby/block/LaunchPadBlock.java
public class LaunchPadBlock extends Block implements EntityBlock, PolymerBlock {
    private final BlockState virtualBlock;

    public LaunchPadBlock(Properties settings, BlockState virtualBlock) {
        super(settings);
        this.virtualBlock = virtualBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return virtualBlock;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl) {
        if (entity.onGround()) {
            var blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof LaunchPadBlockEntity launchPad) {
                entity.setDeltaMovement(getVector(launchPad.getPitch(), entity.getViewYRot(0)).scale(launchPad.getPower()));
                if (entity instanceof ServerPlayer player) {
                    player.connection.send(new ClientboundSetEntityMotionPacket(entity));
                    world.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, 1);
                }
                super.entityInside(state, world, pos, entity, handler, bl);
            }
        }
    }

    private static Vec3 getVector(float pitch, float yaw) {
        double pitchRad = Math.toRadians(pitch);
        double yawRad = Math.toRadians(yaw);

        double horizontal = -Math.cos(pitchRad);
        return new Vec3(
                Math.sin(yawRad) * horizontal,
                Math.sin(pitchRad),
                -Math.cos(yawRad) * horizontal
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LaunchPadBlockEntity(pos, state);
    }
}