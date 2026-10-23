package jewtvet.boathud.mixin;

import jewtvet.boathud.BoatEngineConfig;
import jewtvet.boathud.BoatHudSound;
import jewtvet.boathud.Common;
import jewtvet.boathud.GearKeys;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 只在需要时才改写原版输入，其余情况完全不动（避免影响疾跑 / 走路）。
 *
 * 需要改写的情况只有两种：
 *  1. 驾驶输入 = 自定义按键（把可改键映射成原版输入）
 *  2. 手动挡刹车中（抹掉 backward，避免服务器向后划桨）
 */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void wakespeed$rewriteInput(CallbackInfo ci) {
      BoatEngineConfig cfg = Common.cfg();
      boolean custom = cfg.drive_input == jewtvet.hud.ConfigEnums.drive_input.Custom;
      boolean suppress = BoatHudSound.shouldSuppressReverse();

      // 两种都不需要 -> 完全保留原版行为（疾跑、潜行、走路都不受影响）
      if (!custom && !suppress) {
         return;
      }

      KeyboardInput self = (KeyboardInput)(Object)this;
      Input kp = self.keyPresses;
      if (kp == null) {
         return;
      }

      boolean forward = custom ? GearKeys.THROTTLE.isDown() : kp.forward();
      boolean backward = custom ? GearKeys.BRAKE.isDown() : kp.backward();
      boolean left = custom ? GearKeys.STEER_LEFT.isDown() : kp.left();
      boolean right = custom ? GearKeys.STEER_RIGHT.isDown() : kp.right();

      if (suppress) {
         backward = false;
      }

      self.keyPresses = new Input(forward, backward, left, right,
         kp.jump(), kp.shift(), kp.sprint());
   }
}