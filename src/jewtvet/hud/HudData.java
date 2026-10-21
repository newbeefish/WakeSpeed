package jewtvet.hud;

import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public class HudData {
   public double speed;
   public double driftAngle;
   /** 0..1 engine rpm state, linked to the BoatEngine sound mod when present */
   public float rpm = 0.0F;
   /** 当前是否踩着前进键（转速表限速抖动判定用） */
   public boolean throttle = false;
   /** 0..1 涡轮转速 */
   public float turbo = 0.0F;
   /** 当前挡位（联动音效模组） */
   public int gear = 0;

   public String time_current_lap = "--:--.---";
   public String time_prev_lap = "--:--.---";
   public String race_current_position = "";
   public String race_current_laps = "";
   public String race_current_pits = "";
   public String lastTime = "";
   public boolean isRaceType = false;
   public java.util.ArrayList<Integer> track_times = new java.util.ArrayList<>();

   /** 下船时清空所有状态 */
   public void reset() {
      this.speed = 0.0;
      this.driftAngle = 0.0;
      this.rpm = 0.0F;
      this.throttle = false;
      this.gear = 0;
      this.time_current_lap = "--:--.---";
      this.time_prev_lap = "--:--.---";
      this.race_current_position = "";
      this.race_current_laps = "";
      this.race_current_pits = "";
      this.track_times.clear();
   }

   public void update() {
      if (Common.client == null || Common.client.player == null) {
         return;
      }

      boolean thr = Common.client.player.input.hasForwardImpulse()
                 || Common.client.options.keyUp.isDown();
      this.throttle = thr;
      this.turbo = EngineLink.readTurbo();

      // rpm: prefer the sound mod state so the tach matches the engine audio
      float linked = EngineLink.readRpm();
      int g = EngineLink.readGear();
      if (g != Integer.MIN_VALUE) {
         this.gear = g;    // -1 倒档 / 0 空挡 / 1..6 前进
      }
      if (linked >= 0.0F) {
         if (thr && linked < this.rpm) {
            // holding throttle: never let rpm fall abruptly (filters input flicker)
            this.rpm = Math.max(linked, this.rpm - 0.06F);
         } else {
            this.rpm = linked;
         }
      } else {
         this.rpm = thr
            ? Math.min(1.0F, this.rpm + 0.030F)
            : Math.max(0.0F, this.rpm - 0.020F);
      }

      if (!(Common.client.player.getVehicle() instanceof AbstractBoat boat)) {
         this.speed = 0.0;
         this.driftAngle = 0.0;
         return;
      }

      Vec3 v = boat.getDeltaMovement();
      double hx = v.x;
      double hz = v.z;
      double h2 = hx * hx + hz * hz;
      if (h2 >= 1.0E-4) {
         this.speed = Math.sqrt(h2) * 20.0;
         float yaw = boat.getYRot();
         double rad = Math.toRadians(yaw);
         double fx = -Math.sin(rad);
         double fz = Math.cos(rad);
         double dot = (hx * fx + hz * fz) / Math.sqrt(h2);
         dot = Math.max(-1.0, Math.min(1.0, dot));
         double angle = Math.toDegrees(Math.acos(dot));
         double crossY = fz * hx - fx * hz;
         this.driftAngle = angle * (crossY >= 0.0 ? -1 : 1);

         // 只要实际在向后移动（倒车 / 挂 R 档 / 被撞），就不算漂移
         // 否则速度与船头相反会让夹角接近 180°，误触发漂移计时
         if (dot < 0.0) {
            this.driftAngle = 0.0;
         }
         if (Double.isNaN(this.driftAngle)) {
            this.driftAngle = 0.0;
         }
      } else {
         this.speed = 0.0;
         this.driftAngle = 0.0;
      }
   }
}