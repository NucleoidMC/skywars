package us.potatoboy.skywars.mixin;

import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Final
    @Shadow
    private World world;

    @ModifyConstant(method = "collectBlocksAndDamageEntities", constant = @Constant(doubleValue = 7.0))
    private double reduceDamage(double original) {
        ManagedGameSpace gameSpace = ManagedGameSpace.forWorld(this.world);

        if (gameSpace != null && gameSpace.testRule(SkyWars.REDUCED_EXPLOSION_DAMAGE) == RuleResult.ALLOW) {
            return 4.0D;
        }

        return original;
    }
}
