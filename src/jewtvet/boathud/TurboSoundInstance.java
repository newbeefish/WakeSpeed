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

/** 循环涡轮啸叫：音量/音高由 BoatHudSound.turbo 驱动 */
public class TurboSoundInstance extends AbstractTickableSoundInstance {
   private final LocalPlayer player;
   private boolean dead = false;

   public TurboSoundInstance(LocalPlayer player) {
      super(SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "turbo_whine")),
            SoundSource.PLAYERS, RandomSource.create());
      this.player = player;
      this.looping = true;
      this.relative = false;
      this.attenuation = SoundInstance.Attenuation.NONE;
      this.volume = 0.02F;
      this.pitch = 0.55F;
      Vec3 p = player.position();
      this.x = p.x; this.y = p.y; this.z = p.z;
   }

   @Override
   public void tick() {
      if (this.player == null || this.player.isRemoved()
         || !(this.player.getVehicle() instanceof AbstractBoat boat)
         || BoatHudSound.turbo <= 0.01F) {
         this.dead = true;
         return;
      }
      float t = BoatHudSound.turbo;
      this.volume = (0.010F + t * 0.100F) * Common.cfg().turboGain();
      this.pitch = 0.55F + t * 0.70F;
      Vec3 p = boat.position();
      this.x = p.x; this.y = p.y; this.z = p.z;
   }

   public void kill() { this.dead = true; }

   @Override
   public boolean isStopped() { return this.dead; }
}