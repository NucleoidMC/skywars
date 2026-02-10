package us.potatoboy.skywars.custom.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import us.potatoboy.skywars.custom.SWBlocks;

// Taken from
public class LaunchPadBlockEntity extends BlockEntity {
    private float pitch = 10;
    private float power = 4;

    public LaunchPadBlockEntity(BlockPos pos, BlockState state) {
        super(SWBlocks.LAUNCH_PAD_ENTITY, pos, state);
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getPower() {
        return this.power;
    }

    @Override
    public void saveAdditional(ValueOutput nbt) {
        super.saveAdditional(nbt);
        nbt.putFloat("Pitch", this.pitch);
        nbt.putFloat("Power", this.power);
    }

    @Override
    public void loadAdditional(ValueInput nbt) {
        super.loadAdditional(nbt);
        this.pitch = nbt.getFloatOr("Pitch", 0);
        this.power = nbt.getFloatOr("Power", 0);
    }
}
