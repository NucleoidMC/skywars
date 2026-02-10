package us.potatoboy.skywars.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Redirect(method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;onGround()Z"))
    private boolean setProperties(Entity entity) {
        var gameSpace = GameSpaceManager.get().byWorld(entity.level());

        if (gameSpace != null && gameSpace.getBehavior().testRule(SkyWars.PROJECTILE_PLAYER_MOMENTUM) == EventResult.ALLOW) {
            return true;
        }

        return entity.onGround();
    }
}
