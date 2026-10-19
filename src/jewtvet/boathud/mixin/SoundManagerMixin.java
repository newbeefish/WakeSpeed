package jewtvet.boathud.mixin;

import jewtvet.boathud.BoatHudSound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 屏蔽原版船的划水声（entity.boat.paddle_water / paddle_land）。
 * 玩家在船上时由引擎声替代，避免"引擎声 + 划水声"混在一起。
 */
@Mixin(SoundManager.class)
public class SoundManagerMixin {
   @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;",
           at = @At("HEAD"), cancellable = true)
   private void wakespeed$muteBoatPaddle(SoundInstance sound, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
      if (sound != null && BoatHudSound.shouldMuteVanillaBoatSound(sound.getIdentifier())) {
         cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
      }
   }

   @Inject(method = "playDelayed(Lnet/minecraft/client/resources/sounds/SoundInstance;I)V",
           at = @At("HEAD"), cancellable = true)
   private void wakespeed$muteBoatPaddleDelayed(SoundInstance sound, int delay, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
      if (sound != null && BoatHudSound.shouldMuteVanillaBoatSound(sound.getIdentifier())) {
         ci.cancel();
      }
   }
}