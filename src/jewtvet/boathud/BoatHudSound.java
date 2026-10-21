package jewtvet.boathud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

/**
 * 驾驶 / 音效核心。
 *
 * 自动挡：按速度自动升降挡，S 刹车，停住后继续按 S 倒车。
 * 手动挡：R - N - 1..6，X 升档、Z 降档、C 离合；S 只刹车不倒车；挂 R 后按 W 倒车。
 * 转速：自动挡跟油门；手动挡跟车速（rpm = 车速 / 当前档上限）。
 * 涡轮：倒车也上压；转速越高上压越快；70% 起按比例给动力；满压触发泄压阀（民用）或抗迟滞（拉力）。
 */
public final class BoatHudSound {
   public static float rpm = 0.0F;
   public static float engineGain = 1.0F;
   public static float turbo = 0.0F;
   public static boolean driftActive = false;
   public static float driftIntensity = 0.0F;
   public static boolean brakeActive = false;
   public static float brakeIntensity = 0.0F;
   public static int gear = 0;
   public static boolean evMode = false;

   // ---------- 转速 ----------
   private static final float RPM_UP = 0.030F;
   private static final float RPM_UP_EV = 0.220F;
   private static final float RPM_DOWN_EV = 0.550F;
   private static final float RPM_DOWN = 0.10F;
   private static final float RPM_DOWN_REVERSE = 0.05F;
   private static final float RPM_STOP = 0.02F;
   private static final float IDLE_RPM = 0.10F;
   private static final float REV_MAX_RPM = 0.50F;
   private static final float ENGINE_FADE_OUT = 0.12F;

   // ---------- 涡轮 ----------
   private static final float TURBO_UP = 0.0333F;
   private static final float TURBO_DOWN = 0.0714F;
   private static final float TURBO_STOP = 0.01F;
   private static final float BLOWOFF_MIN_TURBO = 0.98F;
   private static final float TURBO_POWER_FROM = 0.70F;
   private static final double TURBO_POWER_GAIN = 1.10;
   private static final float TURBO_RPM_FACTOR = 1.15F;
   private static final float TURBO_BOOST_FROM = 0.90F;
   private static final float TURBO_BOOST_MULT = 1.6F;
   private static final float ANTI_LAG_HOLD = 0.70F;

   // ---------- 挡位 ----------
   private static final int MAX_GEAR = 6;
   private static final double MAX_SPEED = 44.0;
   private static final int UPSHIFT_TICKS = 12;
   private static final double NEUTRAL_SPEED = 2.0;
   private static final double STOPPED_SPEED = 0.6;
   /** 手动挡各档最高速度（格/秒）：低档密、高档疏 */
   private static final double[] GEAR_MAX = {0.0, 5.0, 11.0, 18.0, 26.0, 34.0, 44.0};
   /** 手动挡倒档最高速度 */
   private static final double REV_SPEED_LIMIT = 8.0;
   /** 手动挡基础加速度（格/刻） */
   private static final double MANUAL_ACCEL = 0.050;
   /** 换挡切点火时长（tick） */
   private static final int SHIFT_CUT_TICKS = 2;

   // ---------- 刹车 ----------
   private static final double BRAKE_DAMP_MANUAL = 0.76;
   private static final double BRAKE_DAMP_AUTO = 0.78;
   private static final double ENGINE_BRAKE_BASE = 0.006;
   private static final double ENGINE_BRAKE_PER_GEAR = 0.0018;
   private static final double BRAKE_SOUND_MIN = 7.0;

   // ---------- 蓝冰 ----------
   private static final double ICE_FRICTION = 0.994;

   // ---------- 漂移 ----------
   private static final double DRIFT_MIN_SPEED = 0.20;
   private static final double DRIFT_MIN_ANGLE = 45.0;
   private static final int WHITE_SMOKE_AFTER_TICKS = 60;
   private static final int BURNOUT_TICKS = 20;
   private static final double BURNOUT_MIN_ANGLE = 60.0;
   private static final double BURNOUT_HALF = 6.0;

   private static EngineSoundInstance engine = null;
   private static TurboSoundInstance turboSound = null;
   private static DriftSoundInstance drift = null;
   private static BrakeSoundInstance brakeSound = null;
   private static SoundEvent clutchSound = null;
   private static SoundEvent blowoffSound = null;
   private static SoundEvent gearSound = null;

   private static boolean prevThrottle = false;
   private static boolean prevClutch = false;
   private static int shiftCutTicks = 0;
   private static int smokeTick = 0;
   private static int driftTicks = 0;
   private static int burnoutTicks = 0;
   private static int driftDir = 0;
   private static int upshiftTicks = 0;
   private static double driftAnchorX = 0.0;
   private static double driftAnchorZ = 0.0;
   private static double prevSpeed = 0.0;
   private static double prevV = 0.0;
   private static double neutralCap = 0.0;
   private static double landSpeed = 0.0;
   private static final int SMOKE_COLOR = 0x121212;
   private static final float SMOKE_SCALE = 1.7F;

   private BoatHudSound() {
   }

   public static void onClientTick(Minecraft mc) {
      if (mc.player == null || !(mc.player.getVehicle() instanceof AbstractBoat boat)) {
         reset();
         return;
      }

      BoatEngineConfig cfg = cfgNow();
      boolean manual = cfg.gearbox == jewtvet.hud.ConfigEnums.gearbox.Manual;
      boolean overIce = cfg.land_as_ice;

      // ---------- 输入 ----------
      boolean customDrive = cfg.drive_input == jewtvet.hud.ConfigEnums.drive_input.Custom;
      boolean throttle = customDrive ? GearKeys.THROTTLE.isDown()
         : (mc.player.input.hasForwardImpulse() || mc.options.keyUp.isDown());
      boolean sHeld = customDrive ? GearKeys.BRAKE.isDown()
         : (mc.options.keyDown.isDown() && !mc.options.keyUp.isDown());
      boolean clutch = manual && GearKeys.CLUTCH.isDown();

      // 刹车优先：踩刹车时油门无效
      if (sHeld) {
         throttle = false;
      }
      boolean rawThrottle = throttle;

      Vec3 vel = boat.getDeltaMovement();
      double spd = Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0;
      double hrad = Math.toRadians(boat.getYRot());
      double fwdX = -Math.sin(hrad);
      double fwdZ = Math.cos(hrad);
      double along = vel.x * fwdX + vel.z * fwdZ;
      boolean movingBackward = sHeld && along < -0.02;

      toggleHotkeys(mc, cfg);

      // ---------- 自定义驾驶 ----------
      if (customDrive) {
         applyCustomDrive(mc, boat, fwdX, fwdZ);
      }

      // ---------- 手动挡换挡 ----------
      if (manual) {
         manualGearTick(mc, cfg);
      }

      // ---------- 蓝冰：所有固体方块摩擦极小 ----------
      if (overIce) {
         applyIce(boat);
      }

      // ---------- 刹车与限速 ----------
      if (manual) {
         manualDynamics(mc, boat, fwdX, fwdZ, sHeld, rawThrottle, clutch, spd, throttle, cfg);
      } else {
         autoDynamics(boat, fwdX, fwdZ, sHeld);
      }

      // ---------- 转速 ----------
      updateRpm(cfg, mc, manual, throttle, rawThrottle, sHeld, clutch, spd);

      // ---------- 涡轮 ----------
      float turboBefore = turbo;
      boolean rally = cfg.driving_mode == jewtvet.hud.ConfigEnums.driving_mode.Rally;
      // 手动挡只有按油门才上压（R 档的油门就是 W）；自动挡 S 倒车也算给油
      boolean turboSpin = manual ? rawThrottle : (rawThrottle || movingBackward);
      float turboUp = TURBO_UP * (0.35F + rpm * TURBO_RPM_FACTOR);
      if (cfg.ev_mode) {
         turbo = 0.0F;
      } else if (turboSpin) {
         turbo = Math.min(1.0F, turbo + turboUp);
      } else if (rally && rpm > 0.45F) {
         turbo = Math.max(ANTI_LAG_HOLD, turbo - TURBO_DOWN * 0.30F);
         if ((mc.level.getGameTime() % 9L) == 0L) {
            playBlowoff(mc, cfg, 0.22F, 0.62F);
         }
      } else {
         turbo = Math.max(0.0F, turbo - TURBO_DOWN);
      }

      // ---------- 收油：离合 + 泄压阀 ----------
      if (!cfg.ev_mode && prevThrottle && !rawThrottle) {
         if (rpm > 0.30F) {
            playClutch(mc, cfg, 0.80F, 0.9F + rpm * 0.25F);
         }
         if (!rally && turboBefore >= BLOWOFF_MIN_TURBO) {
            playBlowoff(mc, cfg, (0.55F + turbo * 0.45F), 0.95F + turbo * 0.20F);
         }
      }
      prevThrottle = rawThrottle;
      prevClutch = clutch;

      // ---------- 刹车胎噪 ----------
      updateBrakeSound(mc, sHeld, spd);

      // ---------- 自动挡换挡 ----------
      if (!manual) {
         autoGearTick(mc, boat, cfg, throttle, sHeld, spd, spd, movingBackward, along);
      }

      // ---------- 引擎声 ----------
      if (rpm > RPM_STOP) {
         engineGain = 1.0F;
         if (engine == null || !mc.getSoundManager().isActive(engine)) {
            clutchSound = null;
            blowoffSound = null;
            gearSound = null;
            engine = new EngineSoundInstance(mc.player);
            mc.getSoundManager().play(engine);
         }
      } else {
         engineGain = Math.max(0.0F, engineGain - ENGINE_FADE_OUT);
         if (engineGain <= 0.02F && engine != null) {
            engine.kill();
            engine = null;
         }
      }

      // ---------- 涡轮声 ----------
      if (turbo > TURBO_STOP) {
         if (turboSound == null || !mc.getSoundManager().isActive(turboSound)) {
            turboSound = new TurboSoundInstance(mc.player);
            mc.getSoundManager().play(turboSound);
         }
      } else if (turboSound != null) {
         turboSound.kill();
         turboSound = null;
      }

      // ---------- 漂移 ----------
      updateDrift(mc, boat);
   }

   // ================= 手动挡 =================

   private static void manualGearTick(Minecraft mc, BoatEngineConfig cfg) {
      // 电车没有变速箱：只有 前进(1) / 空挡(0) / 倒档(-1)
      int maxGear = cfg.ev_mode ? 1 : MAX_GEAR;
      if (GearKeys.SHIFT_UP.consumeClick()) {
         if (gear <= 0) {
            gear = 1;
            neutralCap = 0.0;
         } else if (gear < maxGear) {
            gear++;
         }
         shiftCutTicks = SHIFT_CUT_TICKS;
         rpm = Math.max(IDLE_RPM, rpm * 0.68F);
         playShift(mc, cfg, true);
      }
      if (GearKeys.SHIFT_DOWN.consumeClick()) {
         if (gear > 1) {
            gear--;
         } else if (gear == 1) {
            gear = 0;
            neutralCap = currentSpeed();
         } else if (gear == 0) {
            gear = -1;
         }
         rpm = Math.min(1.0F, rpm * 1.18F);
         playShift(mc, cfg, false);
      }
   }

   private static void manualDynamics(Minecraft mc, AbstractBoat boat, double fwdX, double fwdZ,
                                      boolean sHeld, boolean rawThrottle, boolean clutch,
                                      double spd, boolean throttle, BoatEngineConfig cfg) {
      Vec3 v = boat.getDeltaMovement();
      double along = v.x * fwdX + v.z * fwdZ;

      // 倒档：给油向后推进
      if (gear == -1) {
         if (rawThrottle) {
            // R 档给油：向后推进
            double target = -REV_SPEED_LIMIT / 20.0;
            double d = (target - along) * 0.20;
            boat.setDeltaMovement(v.x + fwdX * d, v.y, v.z + fwdZ * d);
         } else if (sHeld) {
            // R 档按 S：刹车
            boat.setDeltaMovement(v.x * BRAKE_DAMP_MANUAL, v.y, v.z * BRAKE_DAMP_MANUAL);
         }
         return;
      }

      // 空挡：锁住进入时的车速，踩油门不加速
      if (gear == 0) {
         double mag = Math.sqrt(v.x * v.x + v.z * v.z);
         if (mag > neutralCap && mag > 1.0e-4) {
            double k = neutralCap / mag;
            boat.setDeltaMovement(v.x * k, v.y, v.z * k);
         } else {
            neutralCap = mag;
         }
         return;
      }

      // 刹车：按运动方向整体衰减，并抹掉倒退分量
      if (sHeld) {
         double bx = v.x * BRAKE_DAMP_MANUAL;
         double bz = v.z * BRAKE_DAMP_MANUAL;
         double balong = bx * fwdX + bz * fwdZ;
         if (balong < 0.0) {
            bx -= fwdX * balong;
            bz -= fwdZ * balong;
         }
         boat.setDeltaMovement(bx, v.y, bz);
         return;
      }

      // 发动机制动
      if (!rawThrottle && !clutch && spd > 0.4) {
         double decel = ENGINE_BRAKE_BASE + (MAX_GEAR - gear) * ENGINE_BRAKE_PER_GEAR;
         if (along > 0.0) {
            double na = Math.max(0.0, along - decel);
            boat.setDeltaMovement(v.x + fwdX * (na - along), v.y, v.z + fwdZ * (na - along));
         }
      }

      // 档位限速
      // 档位限速（电车无档位，统一按最高档速度）
      double limit = cfgNow().ev_mode ? gearMax(MAX_GEAR) : gearMax(gear);
      if (spd > limit) {
         Vec3 v2 = boat.getDeltaMovement();
         double scale = Math.max(0.82, 1.0 - (spd - limit) * 0.06);
         boat.setDeltaMovement(v2.x * scale, v2.y, v2.z * scale);
      }

      // 档位扭矩：低档起步猛，高档起步无力；涡轮按比例给动力
      if (rawThrottle && !clutch) {
         double vNow = Math.sqrt(vel_mag(boat));
         double dv = vNow - prevV;
         int effGear = cfg.ev_mode ? 1 : gear;                 // 电车按 1 档算扭矩
         double gearTorque = 1.0 / (1.0 + (effGear - 1) * 0.45);
         double rpmFactor = 0.20 + 0.80 * rpm;
         double pull = Math.min(1.0, gearTorque * rpmFactor * 1.6);
         double boost = 1.0 + turboGain(turbo) * TURBO_POWER_GAIN;
         double allowed = MANUAL_ACCEL * pull * boost;
         if (dv > allowed && vNow > 1.0e-4) {
            double target = prevV + allowed;
            double k = Math.max(0.0, target / vNow);
            Vec3 v3 = boat.getDeltaMovement();
            boat.setDeltaMovement(v3.x * k, v3.y, v3.z * k);
            vNow = target;
         }
         prevV = vNow;
      } else {
         prevV = Math.sqrt(vel_mag(boat));
      }
   }

   private static double vel_mag(AbstractBoat boat) {
      Vec3 v = boat.getDeltaMovement();
      return v.x * v.x + v.z * v.z;
   }

   // ================= 自动挡 =================

   private static void autoDynamics(AbstractBoat boat, double fwdX, double fwdZ, boolean sHeld) {
      if (!sHeld) {
         return;
      }
      Vec3 v = boat.getDeltaMovement();
      double along = v.x * fwdX + v.z * fwdZ;
      if (along > 0.01) {
         boat.setDeltaMovement(v.x * BRAKE_DAMP_AUTO, v.y, v.z * BRAKE_DAMP_AUTO);
      }
   }

   private static void autoGearTick(Minecraft mc, AbstractBoat boat, BoatEngineConfig cfg,
                                    boolean throttle, boolean sHeld, double spd, double spd2,
                                    boolean movingBackward, double along) {
      // 电车没有变速箱：D(1) / N(0) / R(-1)
      if (cfg.ev_mode) {
         boolean ani = mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                     || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
         int want;
         if (movingBackward) {
            want = -1;
         } else if (sHeld && spd < STOPPED_SPEED) {
            want = 0;
         } else if (!ani && spd < NEUTRAL_SPEED) {
            want = 0;
         } else {
            want = 1;
         }
         if (gear != want) {
            gear = want;
            upshiftTicks = 0;
         }
         return;
      }
      boolean anyInput = mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                      || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown()
                      || mc.options.keyJump.isDown();

      if (movingBackward) {
         if (gear != -1) {
            gear = -1;
            upshiftTicks = 0;
            playShift(mc, cfg, true);
         }
      } else if (gear == -1) {
         gear = 0;
         upshiftTicks = 0;
      } else if (sHeld && spd < STOPPED_SPEED) {
         if (gear != 0) {
            gear = 0;
            upshiftTicks = 0;
         }
      } else if (!anyInput && spd < NEUTRAL_SPEED) {
         if (gear != 0) {
            gear = 0;
            upshiftTicks = 0;
         }
      } else if (sHeld && spd >= STOPPED_SPEED) {
         upshiftTicks = 0;
      } else {
         int bySpeed = gearForSpeed(spd);
         if (gear <= 0) {
            gear = 1;
            playShift(mc, cfg, true);
         }
         if (bySpeed < gear) {
            gear = bySpeed;
            upshiftTicks = 0;
            playShift(mc, cfg, false);
         }
         if (gear < MAX_GEAR && rpm >= 0.97F) {
            upshiftTicks++;
         } else {
            upshiftTicks = 0;
         }
         if (upshiftTicks >= UPSHIFT_TICKS && spd >= band() * gear && gear < MAX_GEAR) {
            gear++;
            upshiftTicks = 0;
            if (!cfg.ev_mode) {
               rpm = Math.max(0.45F, rpm * 0.72F);
            }
            playShift(mc, cfg, true);
         }
      }
   }

   // ================= 转速 =================

   private static void updateRpm(BoatEngineConfig cfg, Minecraft mc, boolean manual,
                                 boolean throttle, boolean rawThrottle, boolean sHeld,
                                 boolean clutch, double spd) {
      evMode = cfg.ev_mode;

      // 电车没有变速箱：转速直接与车速成正比
      if (cfg.ev_mode) {
         float target = (float)Math.min(1.0, spd / Math.max(1.0, gearMax(MAX_GEAR)));
         rpm += (target - rpm) * 0.28F;
         if (rpm < 0.0F) rpm = 0.0F;
         if (rpm > 1.0F) rpm = 1.0F;
         return;
      }
      if (manual) {
         if (shiftCutTicks > 0) {
            shiftCutTicks--;
         }
         float idleFloor = cfg.ev_mode ? 0.0F : IDLE_RPM;
         float target;
         if (clutch || shiftCutTicks > 0) {
            target = idleFloor;
         } else if (gear == -1) {
            target = rawThrottle ? REV_MAX_RPM : idleFloor;
         } else if (gear == 0) {
            target = rawThrottle ? 1.0F : idleFloor;   // 空挡可自由空踩
         } else {
            float ratio = (float)Math.min(1.0, spd / Math.max(0.25, gearMax(gear)));
            target = cfg.ev_mode ? ratio : (IDLE_RPM + ratio * (1.0F - IDLE_RPM));
         }
         if (!cfg.ev_mode && turbo >= TURBO_BOOST_FROM) {
            target = Math.min(1.0F, target * 1.05F);
         }
         rpm += (target - rpm) * 0.28F;
         if (rpm < 0.0F) rpm = 0.0F;
         if (rpm > 1.0F) rpm = 1.0F;
         return;
      }

      float ceil;
      if (cfg.ev_mode) {
         ceil = throttle ? 1.0F : (sHeld && gear == -1 ? REV_MAX_RPM : 0.0F);
      } else {
         ceil = throttle ? 1.0F : (sHeld && gear == -1 ? REV_MAX_RPM : IDLE_RPM);
      }
      float upRate = cfg.ev_mode ? RPM_UP_EV : RPM_UP;
      if (!cfg.ev_mode && turbo >= TURBO_BOOST_FROM) {
         upRate *= TURBO_BOOST_MULT;
      }
      float downRate = cfg.ev_mode ? RPM_DOWN_EV : (sHeld ? RPM_DOWN_REVERSE : RPM_DOWN);
      if (rpm < ceil) {
         rpm = Math.min(ceil, rpm + upRate);
      } else {
         rpm = Math.max(ceil, rpm - downRate);
      }
   }

   // ================= 蓝冰 / 自定义驾驶 =================

   private static void applyIce(AbstractBoat boat) {
      if (boat.isInWater()) {
         landSpeed = 0.0;
         return;
      }
      Vec3 v = boat.getDeltaMovement();
      double mag = Math.sqrt(v.x * v.x + v.z * v.z);
      if (v.y < -0.12) {
         landSpeed = mag;
         return;
      }
      if (landSpeed > 0.02 && mag > 1.0e-4 && mag < landSpeed) {
         double target = landSpeed * ICE_FRICTION;
         if (mag < target) {
            double k = Math.min(1.0, target / mag);
            boat.setDeltaMovement(v.x * k, v.y, v.z * k);
            mag = mag * k;
         }
      }
      landSpeed = mag;
   }

   private static void applyCustomDrive(Minecraft mc, AbstractBoat boat, double fwdX, double fwdZ) {
      boolean th = GearKeys.THROTTLE.isDown();
      boolean br = GearKeys.BRAKE.isDown();
      boolean left = GearKeys.STEER_LEFT.isDown();
      boolean right = GearKeys.STEER_RIGHT.isDown();
      Vec3 v = boat.getDeltaMovement();
      double along = v.x * fwdX + v.z * fwdZ;
      double steer = (left ? 1.0 : 0.0) - (right ? 1.0 : 0.0);
      if (steer != 0.0) {
         double gain = 0.55 + Math.min(1.0, Math.abs(along) / 0.30) * 2.0;
         boat.setYRot(boat.getYRot() - (float)(steer * gain));
      }
      if (th && !br) {
         double maxAlong = 0.62;
         if (along < maxAlong) {
            double add = Math.min(0.018, maxAlong - along);
            boat.setDeltaMovement(v.x + fwdX * add, v.y, v.z + fwdZ * add);
         }
      }
   }

   // ================= 工具 =================

   private static BoatEngineConfig cfgNow() {
      return Common.cfg();
   }

   private static double turboGain(double t) {
      if (t <= TURBO_POWER_FROM) {
         return 0.0;
      }
      double x = (t - TURBO_POWER_FROM) / (1.0 - TURBO_POWER_FROM);
      if (x > 1.0) {
         x = 1.0;
      }
      return x * x;
   }

   private static double gearMax(int g) {
      int i = g < 1 ? 1 : (g > MAX_GEAR ? MAX_GEAR : g);
      return GEAR_MAX[i];
   }

   private static double band() {
      return MAX_SPEED / MAX_GEAR;
   }

   private static int gearForSpeed(double spd) {
      int g = (int)Math.floor(spd / band()) + 1;
      if (g < 1) g = 1;
      if (g > MAX_GEAR) g = MAX_GEAR;
      return g;
   }

   private static double currentSpeed() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || !(mc.player.getVehicle() instanceof AbstractBoat b)) {
         return 0.0;
      }
      Vec3 v = b.getDeltaMovement();
      return Math.sqrt(v.x * v.x + v.z * v.z);
   }

   private static void toggleHotkeys(Minecraft mc, BoatEngineConfig cfg) {
      boolean dirty = false;
      if (GearKeys.TOGGLE_GEARBOX.consumeClick()) {
         cfg.gearbox = cfg.gearbox == jewtvet.hud.ConfigEnums.gearbox.Manual
            ? jewtvet.hud.ConfigEnums.gearbox.Auto
            : jewtvet.hud.ConfigEnums.gearbox.Manual;
         gear = 0;
         neutralCap = 0.0;
         dirty = true;
      }
      if (GearKeys.TOGGLE_STYLE.consumeClick()) {
         cfg.driving_mode = cfg.driving_mode == jewtvet.hud.ConfigEnums.driving_mode.Rally
            ? jewtvet.hud.ConfigEnums.driving_mode.Civilian
            : jewtvet.hud.ConfigEnums.driving_mode.Rally;
         dirty = true;
      }
      if (GearKeys.TOGGLE_EV.consumeClick()) {
         cfg.ev_mode = !cfg.ev_mode;
         dirty = true;
      }
      if (dirty) {
         me.shedaniel.autoconfig.AutoConfig.getConfigHolder(BoatEngineConfig.class).save();
      }
   }

   private static void updateBrakeSound(Minecraft mc, boolean braking, double spd) {
      boolean want = braking && spd > BRAKE_SOUND_MIN && !driftActive;
      if (want) {
         brakeActive = true;
         brakeIntensity = (float)Math.min(1.0, (spd - BRAKE_SOUND_MIN) / 18.0);
         if (brakeSound == null || !mc.getSoundManager().isActive(brakeSound)) {
            brakeSound = new BrakeSoundInstance(mc.player);
            mc.getSoundManager().play(brakeSound);
         }
      } else {
         brakeActive = false;
         brakeIntensity = 0.0F;
         if (brakeSound != null) {
            brakeSound.kill();
            brakeSound = null;
         }
      }
   }

   private static void playClutch(Minecraft mc, BoatEngineConfig cfg, float vol, float pitch) {
      if (clutchSound == null) {
         clutchSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "clutch"));
      }
      mc.player.playSound(clutchSound, vol * cfg.clutchGain(), pitch);
   }

   private static void playBlowoff(Minecraft mc, BoatEngineConfig cfg, float vol, float pitch) {
      if (blowoffSound == null) {
         blowoffSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "blowoff"));
      }
      mc.player.playSound(blowoffSound, vol * cfg.blowoffGain(), pitch);
   }

   private static void playShift(Minecraft mc, BoatEngineConfig cfg, boolean up) {
      if (cfg.ev_mode) {
         return;
      }
      if (clutchSound == null) {
         clutchSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "clutch"));
      }
      if (gearSound == null) {
         gearSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "gear_shift"));
      }
      boolean manual = cfg.gearbox == jewtvet.hud.ConfigEnums.gearbox.Manual;
      mc.player.playSound(clutchSound, (manual ? 0.55F : 0.40F) * cfg.clutchGain(), up ? 0.95F : 1.06F);
      mc.player.playSound(gearSound, (manual ? 0.88F : 0.60F) * cfg.gearGain(),
         0.85F + Math.max(1, gear) * 0.05F);
   }

   // ================= 漂移 =================

   private static void updateDrift(Minecraft mc, AbstractBoat boat) {
      Vec3 v = boat.getDeltaMovement();
      double hSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
      // 只要实际在向后移动（倒车 / 挂 R 档），就不算漂移
      double dr = Math.toRadians(boat.getYRot());
      double rfx = -Math.sin(dr);
      double rfz = Math.cos(dr);
      boolean reversing = (v.x * rfx + v.z * rfz) < -0.02;
      double angleDiff = 0.0;
      double signedAngle = 0.0;
      if (hSpeed > DRIFT_MIN_SPEED) {
         double heading = Math.toDegrees(Math.atan2(v.z, v.x)) - 90.0;
         signedAngle = Mth.wrapDegrees((float)(boat.getYRot() - heading));
         angleDiff = Math.abs(signedAngle);
      }
      double decel = prevSpeed - hSpeed;
      boolean hardBrake = decel > 0.12 && prevSpeed > 0.50;
      prevSpeed = hSpeed;

      if (reversing) {
         angleDiff = 0.0;
         hardBrake = false;
      }

      if (angleDiff > DRIFT_MIN_ANGLE || hardBrake) {
         driftActive = true;
         driftIntensity = (float)Math.min(1.0, Math.max((angleDiff - DRIFT_MIN_ANGLE) / 45.0, hardBrake ? 0.55 : 0.0));
         int dir = angleDiff > DRIFT_MIN_ANGLE ? (signedAngle > 0.0 ? 1 : -1) : driftDir;
         if (driftTicks == 0 || (dir != driftDir && driftDir != 0)) {
            driftAnchorX = boat.getX();
            driftAnchorZ = boat.getZ();
            driftTicks = 0;
            burnoutTicks = 0;
         }
         driftDir = dir;
         driftTicks++;

         double adx = Math.abs(boat.getX() - driftAnchorX);
         double adz = Math.abs(boat.getZ() - driftAnchorZ);
         boolean inArea = adx <= BURNOUT_HALF && adz <= BURNOUT_HALF;
         boolean hot = inArea && angleDiff > BURNOUT_MIN_ANGLE;
         burnoutTicks = hot ? burnoutTicks + 1 : 0;
         boolean burnout = burnoutTicks >= BURNOUT_TICKS;

         if ((smokeTick % 2) == 0) {
            spawnTireSmoke(mc, boat);
         }
         if (burnout || driftTicks >= WHITE_SMOKE_AFTER_TICKS) {
            if (burnout || (smokeTick % 2) == 1) {
               spawnWhiteSmoke(mc, boat);
            }
         }
         smokeTick++;
         if (driftTicks % BURNOUT_TICKS == 0) {
            driftAnchorX = boat.getX();
            driftAnchorZ = boat.getZ();
         }
         if (drift == null || !mc.getSoundManager().isActive(drift)) {
            drift = new DriftSoundInstance(mc.player);
            mc.getSoundManager().play(drift);
         }
      } else {
         driftActive = false;
         driftIntensity = 0.0F;
         smokeTick = 0;
         driftTicks = 0;
         burnoutTicks = 0;
         driftDir = 0;
         if (drift != null) {
            drift.kill();
            drift = null;
         }
      }
   }

   private static void spawnTireSmoke(Minecraft mc, AbstractBoat boat) {
      if (mc.level == null) return;
      float yaw = boat.getYRot();
      double rad = Math.toRadians(yaw);
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double rx = Math.cos(rad);
      double rz = Math.sin(rad);
      double half = boat.getBbWidth() * 0.42;
      Vec3 p = boat.position();
      DustParticleOptions dust = new DustParticleOptions(SMOKE_COLOR, SMOKE_SCALE);
      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            double x = p.x + fx * half * sz + rx * half * sx;
            double z = p.z + fz * half * sz + rz * half * sx;
            mc.level.addParticle(dust, x, p.y + 0.06, z, rx * sx * 0.02, 0.015, rz * sz * 0.02);
         }
      }
   }

   private static void spawnWhiteSmoke(Minecraft mc, AbstractBoat boat) {
      if (mc.level == null) return;
      float yaw = boat.getYRot();
      double rad = Math.toRadians(yaw);
      double fx = -Math.sin(rad);
      double fz = Math.cos(rad);
      double rx = Math.cos(rad);
      double rz = Math.sin(rad);
      double half = boat.getBbWidth() * 0.42;
      Vec3 p = boat.position();
      for (int sx = -1; sx <= 1; sx += 2) {
         for (int sz = -1; sz <= 1; sz += 2) {
            double x = p.x + fx * half * sz + rx * half * sx;
            double z = p.z + fz * half * sz + rz * half * sx;
            mc.level.addParticle(net.minecraft.core.particles.ParticleTypes.WHITE_SMOKE,
               x, p.y + 0.12, z, rx * sx * 0.05, 0.035, rz * sz * 0.05);
         }
      }
   }

   /**
    * 手动挡刹车时是否要屏蔽「后退」输入。
    * 手动挡下 S 一律只刹车；倒车必须挂 R 档并按油门键。
    */
   public static boolean shouldSuppressReverse() {
      BoatEngineConfig cfg = Common.cfg();
      if (cfg.gearbox != jewtvet.hud.ConfigEnums.gearbox.Manual) {
         return false;
      }
      boolean customDrive = cfg.drive_input == jewtvet.hud.ConfigEnums.drive_input.Custom;
      if (customDrive) {
         return GearKeys.BRAKE.isDown() && !GearKeys.THROTTLE.isDown();
      }
      Minecraft mc = Minecraft.getInstance();
      return mc.options.keyDown.isDown() && !mc.options.keyUp.isDown();
   }

   public static boolean shouldMuteVanillaBoatSound(Object identifier) {
      if (!Common.cfg().mute_vanilla_boat) {
         return false;
      }
      String s = String.valueOf(identifier);
      return s.endsWith("entity.boat.paddle_water") || s.endsWith("entity.boat.paddle_land");
   }

   private static void reset() {
      rpm = 0.0F;
      turbo = 0.0F;
      gear = 0;
      upshiftTicks = 0;
      driftActive = false;
      driftIntensity = 0.0F;
      brakeActive = false;
      brakeIntensity = 0.0F;
      driftTicks = 0;
      burnoutTicks = 0;
      driftDir = 0;
      smokeTick = 0;
      prevSpeed = 0.0;
      prevV = 0.0;
      neutralCap = 0.0;
      landSpeed = 0.0;
      shiftCutTicks = 0;
      engineGain = 1.0F;
      prevThrottle = false;
      prevClutch = false;
      if (engine != null) { engine.kill(); engine = null; }
      if (turboSound != null) { turboSound.kill(); turboSound = null; }
      if (drift != null) { drift.kill(); drift = null; }
      if (brakeSound != null) { brakeSound.kill(); brakeSound = null; }
   }
}
