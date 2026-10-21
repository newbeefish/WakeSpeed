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

/** 高速刹车胎噪（循环），由 BoatHudSound.brakeActive 控制起停。 */
public class BrakeSoundInstance extends AbstractTickableSoundInstance {
   private final LocalPlayer player;
   private boolean dead = false;

   public BrakeSoundInstance(LocalPlayer player) {
      super(
         SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "drift_skid")),
         SoundSource.PLAYERS,
         RandomSource.create()
      );
      this.player = player;
      this.looping = true;
      this.relative = false;
      this.attenuation = SoundInstance.Attenuation.NONE;
      this.volume = 0.40F;
      this.pitch = 0.78F;
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
         || !BoatHudSound.brakeActive) {
         this.dead = true;
         return;
      }
      float k = BoatHudSound.brakeIntensity;
      this.volume = (0.22F + k * 0.50F) * Common.cfg().driftGain();
      this.pitch = 0.72F + k * 0.22F;
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