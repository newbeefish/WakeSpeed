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

/** 循环引擎音：音高/音量由 BoatHudSound.rpm 驱动，2 秒整周期音频循环无缝。 */
public class EngineSoundInstance extends AbstractTickableSoundInstance {
   private final LocalPlayer player;
   private boolean dead = false;

   public EngineSoundInstance(LocalPlayer player) {
      super(
         SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed",
            Common.cfg().ev_mode ? "motor" : "engine_loop")),
         SoundSource.PLAYERS,
         RandomSource.create()
      );
      this.player = player;
      this.looping = true;
      this.relative = false;
      this.attenuation = SoundInstance.Attenuation.NONE;
      this.volume = 0.07F;
      this.pitch = 0.70F;
      Vec3 p = player.position();
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   @Override
   public void tick() {
      if (this.player == null || this.player.isRemoved()
         || !(this.player.getVehicle() instanceof AbstractBoat boat)
         || BoatHudSound.engineGain <= 0.02F) {
         this.dead = true;
         return;
      }
      float rpm = BoatHudSound.rpm;
      // 怠速区（rpm < 0.30）叠加轻微抖动，让引擎"活着"
      float idleMix = 1.0F - Math.min(1.0F, rpm / 0.30F);
      double sec = System.nanoTime() / 1.0e9;
      float wobble = (float)(Math.sin(sec * 8.7) * 0.0035 + Math.sin(sec * 14.3) * 0.0022) * idleMix;

      this.volume = (0.06F + rpm * 0.38F) * BoatHudSound.engineGain * (1.0F + wobble * 4.0F) * Common.cfg().engineGain();
      this.pitch = (0.70F + rpm * 0.75F + wobble * 2.0F) * Common.cfg().enginePitch();
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