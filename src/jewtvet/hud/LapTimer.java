package jewtvet.hud;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 过线计时：判定船是否经过世界里放置的【线】（绊线 tripwire / 绊线钩 tripwire_hook）。
 *
 * 每 tick 把船上一帧到这一帧的移动轨迹按 0.2 格采样，
 * 检查采样点附近（-2..+1 格高度）是否有绊线方块，
 * 所以船速再快一帧跨过 1~2 格也不会漏判。
 *
 * 第一次经过线：开始计时。
 * 之后每次经过线：记录上一圈，TIME 归零重计。
 */
public final class LapTimer {
   public static String current = "--:--.---";
   public static String previous = "--:--.---";
   public static int lapCount = 0;
   public static boolean timing = false;

   private static final double STEP = 0.2;
   private static final int COOLDOWN_TICKS = 12;

   private static double prevX = 0.0;
   private static double prevY = 0.0;
   private static double prevZ = 0.0;
   private static boolean hasPrev = false;
   private static boolean wasOnLine = false;
   private static int cooldown = 0;
   private static int dbgTicks = 0;
   private static long lapStartNs = 0L;

   private LapTimer() {
   }

   /** 清零：下次过线重新开始计时 */
   public static void reset() {
      current = "--:--.---";
      previous = "--:--.---";
      lapCount = 0;
      timing = false;
      lapStartNs = 0L;
      wasOnLine = false;
      cooldown = 0;
   }

   /** 每 tick 调用；返回本 tick 是否发生了有效过线 */
   public static boolean tick(Level level, double x, double y, double z) {
      if (cooldown > 0) {
         cooldown--;
      }

      if (!hasPrev) {
         prevX = x;
         prevY = y;
         prevZ = z;
         hasPrev = true;
         wasOnLine = hit(level, x, y, z);
         return false;
      }

      boolean crossed = sweep(level, prevX, prevY, prevZ, x, y, z);
      boolean nowOn = hit(level, x, y, z);


      boolean lap = false;
      if ((crossed || nowOn) && !wasOnLine && cooldown == 0) {
         long now = System.nanoTime();
         if (!timing) {
            timing = true;
            lapStartNs = now;
            current = format(0L);
            lapCount = 0;
         } else {
            previous = format(now - lapStartNs);
            lapStartNs = now;
            current = format(0L);
            lapCount++;
         }
         cooldown = COOLDOWN_TICKS;
         lap = true;
         System.out.println("[wakespeed] lap " + lapCount + " = " + previous);
      }

      wasOnLine = nowOn;
      prevX = x;
      prevY = y;
      prevZ = z;

      if (timing && lapStartNs != 0L) {
         current = format(System.nanoTime() - lapStartNs);
      }
      return lap;
   }

   /** 扫描半径内绊线方块数量（诊断用） */
   private static int scanNear(Level level, double x, double y, double z, int r) {
      int cnt = 0;
      int bx = Mth.floor(x);
      int by = Mth.floor(y);
      int bz = Mth.floor(z);
      for (int ax = -r; ax <= r; ax++) {
         for (int ay = -3; ay <= 2; ay++) {
            for (int az = -r; az <= r; az++) {
               Block b = level.getBlockState(new BlockPos(bx + ax, by + ay, bz + az)).getBlock();
               if (b == Blocks.TRIPWIRE || b == Blocks.TRIPWIRE_HOOK) {
                  cnt++;
               }
            }
         }
      }
      return cnt;
   }

   private static boolean hit(Level level, double x, double y, double z) {
      int bx = Mth.floor(x);
      int by = Mth.floor(y);
      int bz = Mth.floor(z);
      for (int d = -2; d <= 1; d++) {
         Block b = level.getBlockState(new BlockPos(bx, by + d, bz)).getBlock();
         if (b == Blocks.TRIPWIRE || b == Blocks.TRIPWIRE_HOOK) {
            return true;
         }
      }
      return false;
   }

   private static boolean sweep(Level level, double x0, double y0, double z0,
                                double x1, double y1, double z1) {
      double dx = x1 - x0;
      double dy = y1 - y0;
      double dz = z1 - z0;
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      int steps = Math.max(1, (int)Math.ceil(dist / STEP));
      for (int i = 1; i <= steps; i++) {
         double t = (double)i / (double)steps;
         if (hit(level, x0 + dx * t, y0 + dy * t, z0 + dz * t)) {
            return true;
         }
      }
      return false;
   }

   private static String format(long ns) {
      if (ns < 0) {
         ns = 0;
      }
      long ms = ns / 1_000_000L;
      long m = ms / 60000L;
      long s = (ms / 1000L) % 60L;
      long r = ms % 1000L;
      if (m > 0) {
         return String.format("%d:%02d.%03d", m, s, r);
      }
      return String.format("%d.%03d", s, r);
   }
}