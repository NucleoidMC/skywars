package us.potatoboy.skywars.custom.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
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
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt = super.writeNbt(nbt);
        nbt.putFloat("Pitch", this.pitch);
        nbt.putFloat("Power", this.power);

        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.pitch = nbt.getFloat("Pitch");
        this.power = nbt.getFloat("Power");
    }
}
