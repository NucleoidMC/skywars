package us.potatoboy.skywars.mixin;

import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Final
    @Shadow
    private World world;

    @ModifyConstant(method = "collectBlocksAndDamageEntities", constant = @Constant(doubleValue = 7.0))
    private double reduceDamage(double original) {
        var gameSpace = GameSpaceManager.get().byWorld(this.world);

        if (gameSpace != null && gameSpace.getBehavior().testRule(SkyWars.REDUCED_EXPLOSION_DAMAGE) == ActionResult.SUCCESS) {
            return 4.0D;
        }

        return original;
    }
}
