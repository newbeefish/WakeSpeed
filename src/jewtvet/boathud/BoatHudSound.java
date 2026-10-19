package jewtvet.boathud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

/**
 * 燃油车音效控制：引擎 / 涡轮 / 泄压阀 / 离合 / 挂挡 / 漂移。
 *
 * 引擎：转速状态机（rpm 0..1）。油门 -> 1.0；松手 -> 平滑落到怠速；倒档后退 -> 上限 0.5。
 * 涡轮：独立转速，1.5s 升到满，0.7s 降停。
 * 换挡：满转维持 0.6s 且速度到位 -> 升挡（离合+挂挡声，转速掉再回升）；速度掉出区间 -> 立即降挡。
 * 漂移：边沿触发；10x10 内同向满 1 秒判烧胎，产生黑颗粒与白烟。
 */
public final class BoatHudSound {
   public static float rpm = 0.0F;
   public static float engineGain = 1.0F;
   public static float turbo = 0.0F;
   public static boolean driftActive = false;
   public static float driftIntensity = 0.0F;
   public static int gear = 0;
   /** 当前是否处于电车模式（供 HUD 判断是否做转速抖动） */
   public static boolean evMode = false;

   private static final float RPM_UP = 0.030F;
   /** 电车升转速率：电机扭矩瞬间输出，约 0.23 秒到满转（油车的 7 倍） */
   private static final float RPM_UP_EV = 0.220F;
   /** 电车收电门回落速率：约 2 tick（0.1 秒）归零，只剩声音淡出做过渡 */
   private static final float RPM_DOWN_EV = 0.550F;
   private static final float RPM_DOWN = 0.10F;
   private static final float RPM_DOWN_REVERSE = 0.05F;
   private static final float RPM_STOP = 0.02F;
   private static final float IDLE_RPM = 0.10F;
   private static final float REV_MAX_RPM = 0.50F;
   private static final float ENGINE_FADE_OUT = 0.12F;
   private static final float BLOWOFF_MIN_RPM = 5000.0F / 8000.0F;

   private static final float TURBO_UP = 0.0333F;
   private static final float TURBO_DOWN = 0.0714F;
   private static final float TURBO_STOP = 0.01F;

   private static final int MAX_GEAR = 6;
   private static final double MAX_SPEED = 40.0;
   private static final int UPSHIFT_TICKS = 12;
   private static final double NEUTRAL_SPEED = 2.0;
   private static final double STOPPED_SPEED = 0.6;

   private static final double DRIFT_MIN_SPEED = 0.20;
   private static final double DRIFT_MIN_ANGLE = 45.0;
   private static final int WHITE_SMOKE_AFTER_TICKS = 60;
   private static final int BURNOUT_TICKS = 20;
   private static final double BURNOUT_MIN_ANGLE = 60.0;
   private static final double BURNOUT_HALF = 6.0;

   private static EngineSoundInstance engine = null;
   private static TurboSoundInstance turboSound = null;
   private static DriftSoundInstance drift = null;
   private static SoundEvent clutchSound = null;
   private static SoundEvent blowoffSound = null;
   private static SoundEvent gearSound = null;
   /** 上一次已知的 SoundManager 是否可用，用于检测资源重载后重建音效缓存 */
   private static boolean sndAlive = true;
   private static boolean prevThrottle = false;
   private static int smokeTick = 0;
   private static int driftTicks = 0;
   private static int burnoutTicks = 0;
   private static int driftDir = 0;
   private static int upshiftTicks = 0;
   private static double driftAnchorX = 0.0;
   private static double driftAnchorZ = 0.0;
   private static double prevSpeed = 0.0;
   private static final int SMOKE_COLOR = 0x121212;
   private static final float SMOKE_SCALE = 1.7F;

   private BoatHudSound() {
   }

   public static void onClientTick(Minecraft mc) {
      if (mc.player == null || !(mc.player.getVehicle() instanceof AbstractBoat boat)) {
         reset();
         return;
      }

      boolean throttle = mc.player.input.hasForwardImpulse() || mc.options.keyUp.isDown();
      boolean sHeld = mc.options.keyDown.isDown() && !mc.options.keyUp.isDown();

      Vec3 vel = boat.getDeltaMovement();
      double spd = Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0;
      double hrad = Math.toRadians(boat.getYRot());
      double fwdX = -Math.sin(hrad);
      double fwdZ = Math.cos(hrad);
      double along = vel.x * fwdX + vel.z * fwdZ;
      boolean movingBackward = sHeld && along < -0.02;
      boolean revThrottle = movingBackward;

      // ---------- 引擎 / 电机转速 ----------
      evMode = cfgNow().ev_mode;
      BoatEngineConfig cfg = cfgNow();
      float ceil;

      if (cfg.ev_mode) {
         // 电车没有怠速，也没有动能回收（赛车配置）：
         //   停止 / 滑行  -> 0（完全静音）
         //   踩电门       -> 拉满 1.0
         //   倒档后退     -> 一半
         ceil = throttle ? 1.0F : (revThrottle ? REV_MAX_RPM : 0.0F);
      } else {
         // 燃油车：怠速 0.10，油门到底 1.0，倒档 0.5
         ceil = throttle ? 1.0F : (revThrottle ? REV_MAX_RPM : IDLE_RPM);
      }

      float upRate = cfg.ev_mode ? RPM_UP_EV : RPM_UP;
      float downRate = cfg.ev_mode ? RPM_DOWN_EV : (sHeld ? RPM_DOWN_REVERSE : RPM_DOWN);

      // 回落下限必须是 ceil（目标值），不能是怠速/0：
      // 用怠速会让 rpm 冲上 1.0 后立刻掉回 0.90，在 0.90~1.00 之间死循环震荡，
      // 导致 rpm>=0.97 只成立 2 个 tick -> 升挡计时永远清零 -> 挡位卡在 1，
      // 同时 HUD 读到震荡值 -> 表针与音高一起抖。
      if (rpm < ceil) {
         rpm = Math.min(ceil, rpm + upRate);
      } else {
         rpm = Math.max(ceil, rpm - downRate);
      }

      // ---------- 涡轮转速 ----------
      turbo = cfgNow().ev_mode
         ? 0.0F
         : (throttle ? Math.min(1.0F, turbo + TURBO_UP) : Math.max(0.0F, turbo - TURBO_DOWN));

      // ---------- 收油：离合声 + 泄压阀 ----------
      if (!cfgNow().ev_mode && prevThrottle && !throttle) {
         if (rpm > 0.30F) {
            if (clutchSound == null) {
               clutchSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "clutch"));
            }
            mc.player.playSound(clutchSound, 0.80F * cfg.clutchGain(), 0.9F + rpm * 0.25F);
         }
         if (rpm >= BLOWOFF_MIN_RPM) {
            if (blowoffSound == null) {
               blowoffSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "blowoff"));
            }
            mc.player.playSound(blowoffSound, (0.55F + turbo * 0.45F) * cfg.blowoffGain(),
               0.95F + turbo * 0.20F);
         }
      }
      prevThrottle = throttle;

      // ---------- 挡位（含换挡音） ----------
      boolean anyInput = mc.options.keyUp.isDown() || mc.options.keyDown.isDown()
                      || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown()
                      || mc.options.keyJump.isDown();

      if (movingBackward) {
         if (gear != -1) {
            gear = -1;
            upshiftTicks = 0;
            playShift(mc, true);
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
            playShift(mc, true);
         }
         if (bySpeed < gear) {
            gear = bySpeed;
            upshiftTicks = 0;
            playShift(mc, false);
         }
         if (gear < MAX_GEAR && rpm >= 0.97F) {
            upshiftTicks++;
         } else {
            upshiftTicks = 0;
         }
         boolean speedReady = spd >= band() * gear;
         if (upshiftTicks >= UPSHIFT_TICKS && speedReady && gear < MAX_GEAR) {
            gear++;
            upshiftTicks = 0;
            if (!Common.cfg().ev_mode) { rpm = Math.max(0.45F, rpm * 0.72F); }
            playShift(mc, true);
         }
      }

      // ---------- 引擎循环音 ----------
      if (rpm > RPM_STOP) {
         engineGain = 1.0F;
         if (engine == null || !mc.getSoundManager().isActive(engine)) {
         // 资源重载后一次性音效的缓存也作废，下次播放时重新解析
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

      // ---------- 涡轮循环音 ----------
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
      Vec3 v = boat.getDeltaMovement();
      double hSpeed = Math.sqrt(v.x * v.x + v.z * v.z);
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

      if (movingBackward) {
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

         if (hot) {
            burnoutTicks++;
         } else {
            burnoutTicks = 0;
         }
         boolean burnout = burnoutTicks >= BURNOUT_TICKS;

         if ((smokeTick % 2) == 0) {
            spawnTireSmoke(mc, boat);
         }
         if (burnout) {
            spawnWhiteSmoke(mc, boat);
         } else if (driftTicks >= WHITE_SMOKE_AFTER_TICKS && (smokeTick % 2) == 1) {
            spawnWhiteSmoke(mc, boat);
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

   private static BoatEngineConfig cfgNow() {
      return Common.cfg();
   }

   private static void playShift(Minecraft mc, boolean up) {
      BoatEngineConfig cfg = Common.cfg();
      if (cfg.ev_mode) {
         return;   // 电车无离合/挂挡
      }
      if (clutchSound == null) {
         clutchSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "clutch"));
      }
      if (gearSound == null) {
         gearSound = SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("wakespeed", "gear_shift"));
      }
      mc.player.playSound(clutchSound, 0.50F * cfg.clutchGain(), up ? 0.95F : 1.06F);
      mc.player.playSound(gearSound, 0.70F * cfg.gearGain(), 0.85F + Math.max(1, gear) * 0.05F);
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

   public static boolean shouldMuteVanillaBoatSound(Object identifier) {
      if (!Common.cfg().mute_vanilla_boat || rpm <= 0.001F) {
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
      driftTicks = 0;
      burnoutTicks = 0;
      driftDir = 0;
      smokeTick = 0;
      prevSpeed = 0.0;
      engineGain = 1.0F;
      prevThrottle = false;
      if (engine != null) { engine.kill(); engine = null; }
      if (turboSound != null) { turboSound.kill(); turboSound = null; }
      if (drift != null) { drift.kill(); drift = null; }
   }
}
