package jewtvet.boathud.mixin;

import jewtvet.boathud.BoatHudSound;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 手动挡刹车时屏蔽「后退」按键状态。
 *
 * 原版船靠按键状态划桨，即使客户端把速度压到 0，
 * 服务器仍会按 S 施加向后的桨力，于是刹停后又开始倒退。
 * 在 KeyboardInput.tick() 结尾把 backward 抹掉，服务器就不会倒划。
 */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
   @Inject(method = "tick", at = @At("TAIL"))
   private void wakespeed$suppressReverseWhileBraking(CallbackInfo ci) {
      if (!BoatHudSound.shouldSuppressReverse()) {
         return;
      }
      KeyboardInput self = (KeyboardInput)(Object)this;
      Input kp = self.keyPresses;
      if (kp != null && kp.backward()) {
         self.keyPresses = new Input(kp.forward(), false, kp.left(), kp.right(),
            kp.jump(), kp.shift(), kp.sprint());
      }
   }
}