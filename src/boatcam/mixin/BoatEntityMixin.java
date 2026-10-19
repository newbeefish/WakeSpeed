package boatcam.mixin;

import boatcam.config.BoatCamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public class BoatEntityMixin {
   /**
    * 对应 1.21.11 的 clampPassengerYaw（26.1.2 叫 clampRotation）。
    *
    * 原版逻辑：把乘客的 bodyYaw 设成船的朝向，然后把头部相对角度限制在 ±105°。
    * 关闭 turnLimit 时：保留"身体对齐船身"，但跳过 ±105° 限制，让头部能自由转。
    *
    * 注意：
    *  - 必须用 setYBodyRot（身体朝向），不能用 setYRot（视线朝向），否则身体会跟着视线转、坐不正，
    *    并且会每帧覆盖 BoatCam 自己算出的视角。
    *  - 不能注入 positionRider，那会取消载客定位，导致船开走而玩家留在原地。
    */
   @Inject(method = "clampRotation(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
   private void updatePassengerPosition(Entity entity, CallbackInfo info) {
      if (entity.equals(Minecraft.getInstance().player) && BoatCamConfig.getConfig().isTurnLimitDisabled()) {
         float yaw = ((Entity)(Object)this).getYRot();
         entity.setYBodyRot(yaw);
         entity.setYHeadRot(entity.getYRot());
         info.cancel();
      }
   }
}
