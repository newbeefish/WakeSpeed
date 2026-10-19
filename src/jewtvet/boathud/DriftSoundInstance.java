package jewtvet.boathud;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

/**
 * 循环漂移胎叫：由 BoatHudSound.driftActive 直接控制起停。
 * 越过 45° 立刻开始，回到 45° 以内立刻停止（不是一次性音效）。
 */
public class DriftSoundInstance extends AbstractTickableSoundInstance {
   private final LocalPlayer player;
   private boolean dead = false;

   public DriftSoundInstance(LocalPlayer player) {
      super(
         SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "drift_skid")),
         SoundSource.PLAYERS,
         RandomSource.create()
      );
      this.player = player;
      this.looping = true;
      this.relative = false;
      this.attenuation = SoundInstance.Attenuation.NONE;
      this.volume = 0.55F;
      this.pitch = 1.0F;
      Vec3 p = player.position();
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   @Override
   public void tick() {
      if (this.player == null
         || this.player.isRemoved()
         || !(this.player.getVehicle() instanceof AbstractBoat boat)
         || !BoatHudSound.driftActive) {
         this.dead = true;
         return;
      }
      float k = BoatHudSound.driftIntensity;
      this.volume = (0.40F + k * 0.55F) * Common.cfg().driftGain();
      this.pitch = 0.92F + k * 0.30F;
      Vec3 p = boat.position();
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public void kill() {
      this.dead = true;
   }

   @Override
   public boolean isStopped() {
      return this.dead;
   }
}
