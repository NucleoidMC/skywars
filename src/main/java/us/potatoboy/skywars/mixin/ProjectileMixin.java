package us.potatoboy.skywars.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import us.potatoboy.skywars.SkyWars;
import xyz.nucleoid.plasmid.api.game.GameSpaceManager;
import xyz.nucleoid.stimuli.event.EventResult;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileMixin {
    @Redirect(method = "setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isOnGround()Z"))
    private boolean setProperties(Entity entity) {
        var gameSpace = GameSpaceManager.get().byWorld(entity.getWorld());

        if (gameSpace != null && gameSpace.getBehavior().testRule(SkyWars.PROJECTILE_PLAYER_MOMENTUM) == EventResult.ALLOW) {
            return true;
        }

        return entity.isOnGround();
    }
}
