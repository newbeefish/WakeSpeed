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
 * 在 KeyboardInput.tick() 结束时改写按键状态：
 *
 * 1. 自定义驾驶模式：把「油门/刹车/左转/右转」四个可改键写进原版输入，
 *    物理完全交给原版，手感与直接按 WASD 一致。
 * 2. 手动挡刹车：抹掉 backward 标志，服务器不会向后划桨，刹停即停。
 */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void wakespeed$rewriteInput(CallbackInfo ci) {
      BoatEngineConfig cfg = Common.cfg();
      KeyboardInput self = (KeyboardInput)(Object)this;
      Input kp = self.keyPresses;
      if (kp == null) {
         return;
      }

      boolean custom = cfg.drive_input == jewtvet.hud.ConfigEnums.drive_input.Custom;
      boolean forward = custom ? GearKeys.THROTTLE.isDown() : kp.forward();
      boolean backward = custom ? GearKeys.BRAKE.isDown() : kp.backward();
      boolean left = custom ? GearKeys.STEER_LEFT.isDown() : kp.left();
      boolean right = custom ? GearKeys.STEER_RIGHT.isDown() : kp.right();

      // 手动挡刹车：禁止后退输入
      if (backward && BoatHudSound.shouldSuppressReverse()) {
         backward = false;
      }

      if (forward != kp.forward() || backward != kp.backward()
         || left != kp.left() || right != kp.right()) {
         self.keyPresses = new Input(forward, backward, left, right,
            kp.jump(), kp.shift(), kp.sprint());
      }
   }
}