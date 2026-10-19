package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Procedural tachometer.
 * Scale: 0 .. 8000 rpm (idle offset 800), numerals every 1000, redline from 7000.
 *
 * 注意：26.x 的 GUI 是延迟批处理渲染，blit/fill 与 text 分属不同管线批次，
 * 不调用 nextStratum() 的话文字会被后画的贴图/填充盖住。
 * 所以这里把绘制拆成两个 stratum：先全部填充，再全部文字。
 */
public final class Tachometer {
   public static final int MAX_RPM  = 8000;
   public static final int IDLE_RPM = 800;
   private static final int MAJOR_STEP = 1000;
   private static final int MINOR_STEP = 500;
   private static final int REDLINE_FROM = 7000;

   private static final int BG        = 0xB0141010;
   private static final int BG_RIM    = 0xFF2A2A2A;
   private static final int TICK      = 0xFFD8D8D8;
   private static final int TICK_MINOR= 0xFF7A7A7A;
   private static final int REDLINE   = 0xFFE02424;
   private static final int NEEDLE    = 0xFFF0F0F0;
   private static final int NEEDLE_BASE = 0xFFB02020;
   private static final int TEXT_DIM  = 0xFF9A9A9A;
   private static final int TEXT_HI   = 0xFFFFFFFF;
   private static final int GEAR_BOX  = 0xFF1A1A1A;
   private static final int GEAR_EDGE = 0xFF555555;

   private static final double START_DEG = -120.0;
   private static final double SWEEP_DEG = 240.0;
   private static final double R_OUT = 48.0;

   private Tachometer() {
   }

   private static double angleFor(double rpm) {
      return Math.toRadians(START_DEG + SWEEP_DEG * (rpm / (double)MAX_RPM));
   }

   public static void render(GuiGraphicsExtractor g, Minecraft mc, float rpmValue, int gear, float turbo, boolean showTurbo) {
      if (rpmValue < 0.0F) rpmValue = 0.0F;
      if (rpmValue > MAX_RPM) rpmValue = MAX_RPM;

      double cx = 50.0;
      double cy = 50.0;

      // ========== stratum 1: 全部填充 ==========
      for (int dy = -(int)R_OUT; dy <= (int)R_OUT; dy++) {
         double hw = Math.sqrt(Math.max(0.0, R_OUT * R_OUT - (double)dy * dy));
         g.fill((int)(cx - hw), (int)cy + dy, (int)(cx + hw) + 1, (int)cy + dy + 1, BG);
      }
      for (int dy = -(int)R_OUT; dy <= (int)R_OUT; dy++) {
         double hw = Math.sqrt(Math.max(0.0, R_OUT * R_OUT - (double)dy * dy));
         int x1 = (int)(cx - hw), x2 = (int)(cx + hw) + 1;
         double innerR = (R_OUT - 2.0) * (R_OUT - 2.0) - (double)dy * dy;
         if (innerR <= 0.0) {
            g.fill(x1, (int)cy + dy, x2, (int)cy + dy + 1, BG_RIM);
         } else {
            double hwIn = Math.sqrt(innerR);
            int ix1 = (int)(cx - hwIn), ix2 = (int)(cx + hwIn) + 1;
            g.fill(x1, (int)cy + dy, ix1, (int)cy + dy + 1, BG_RIM);
            g.fill(ix2, (int)cy + dy, x2, (int)cy + dy + 1, BG_RIM);
         }
      }

      Matrix3x2fStack m = g.pose();

      for (int rpm = 0; rpm <= MAX_RPM; rpm += MAJOR_STEP) {
         double ang = angleFor(rpm);
         boolean red = rpm >= REDLINE_FROM;
         m.pushMatrix();
         m.translate((float)cx, (float)cy);
         m.rotate((float)ang);
         g.fill(-1, -46, 1, -37, red ? REDLINE : TICK);
         m.popMatrix();
      }
      for (int rpm = MINOR_STEP; rpm < MAX_RPM; rpm += MINOR_STEP) {
         if (rpm % MAJOR_STEP == 0) continue;
         double ang = angleFor(rpm);
         boolean red = rpm >= REDLINE_FROM;
         m.pushMatrix();
         m.translate((float)cx, (float)cy);
         m.rotate((float)ang);
         g.fill(0, -45, 1, -39, red ? REDLINE : TICK_MINOR);
         m.popMatrix();
      }

      // needle
      m.pushMatrix();
      m.translate((float)cx, (float)cy);
      m.rotate((float)angleFor(rpmValue));
      g.fill(-1, -44, 1, -8, NEEDLE);
      g.fill(-2, -8, 2, 2, NEEDLE_BASE);
      m.popMatrix();
      // hub
      g.fill((int)cx - 3, (int)cy - 3, (int)cx + 3, (int)cy + 3, 0xFF101010);

      // gear box
      int gx1 = (int)cx - 10;
      int gx2 = (int)cx + 10;
      int gy1 = (int)cy + 24;
      int gy2 = (int)cy + 40;
      g.fill(gx1, gy1, gx2, gy2, GEAR_BOX);
      g.fill(gx1, gy1, gx2, gy1 + 1, GEAR_EDGE);
      g.fill(gx1, gy2 - 1, gx2, gy2, GEAR_EDGE);
      g.fill(gx1, gy1, gx1 + 1, gy2, GEAR_EDGE);
      g.fill(gx2 - 1, gy1, gx2, gy2, GEAR_EDGE);

      // 涡轮指示盘：指针转轴上方的小圆盘，扇形填充（满压全白，EV 模式不显示）
      float tv = turbo;
      if (tv < 0.0F) tv = 0.0F;
      if (tv > 1.0F) tv = 1.0F;
      int discCx = (int)cx;
      int discCy = (int)cy - 13;
      int discR = 7;
      if (showTurbo) {
         // 底盘
         for (int dy = -discR; dy <= discR; dy++) {
            int hw = (int)Math.sqrt(Math.max(0, discR * discR - dy * dy));
            g.fill(discCx - hw, discCy + dy, discCx + hw + 1, discCy + dy + 1, 0xFF141414);
         }
         // 扇形填充（从正上方顺时针）
         fillSector(g, discCx, discCy, discR, discR - 1, tv,
            tv >= 0.995F ? 0xFFF4F4F4 : 0xFFF2F2F2);
         // 外圈
         for (int deg = 0; deg < 360; deg += 4) {
            double a = Math.toRadians(deg);
            int px = discCx + (int)Math.round(Math.sin(a) * (discR + 1));
            int py = discCy - (int)Math.round(Math.cos(a) * (discR + 1));
            g.fill(px, py, px + 1, py + 1, 0xFF3A3A3A);
         }
      }

      // ========== stratum 2: 全部文字 ==========
      g.nextStratum();

      // 圆形表不显示涡轮数字：小圆盘的扇形本身就够直观，数字会挡住刻度

      for (int rpm = 0; rpm <= MAX_RPM; rpm += MAJOR_STEP) {
         double ang = angleFor(rpm);
         double tx = cx + Math.sin(ang) * 33.0;
         double ty = cy - Math.cos(ang) * 33.0;
         String s = Integer.toString(rpm / 1000);
         int col = rpm >= REDLINE_FROM ? REDLINE : TICK;
         g.text(mc.font, s, (int)tx - 2, (int)ty - 4, col, false);
      }

      g.text(mc.font, "x1000", 6, 4, TEXT_DIM, false);

      String big = Integer.toString((int)Math.round(rpmValue));
      g.text(mc.font, big, (int)cx - mc.font.width(big) / 2, (int)cy + 11, TEXT_HI, false);

      String gs = gear < 0 ? "R" : (gear == 0 ? "N" : Integer.toString(gear));
      int gcol = gear >= 6 ? REDLINE : (gear < 0 ? 0xFFFF9A3C : TEXT_HI);
      Matrix3x2fStack gm = g.pose();
      gm.pushMatrix();
      gm.translate((float)cx, (float)(gy1 + 3));
      gm.scale(2.0F, 2.0F);
      g.text(mc.font, gs, -mc.font.width(gs) / 2, 0, gcol, false);
      gm.popMatrix();
   }

   /**
    * 在圆盘内填充扇形（从正上方顺时针扫过 fraction*360 度）。
    * 按列扫描并合并连续的亮像素，减少 fill 调用次数。
    */
   private static void fillSector(GuiGraphicsExtractor g, int cx, int cy, int rOuter, int rInner,
                                  float fraction, int color) {
      if (fraction <= 0.0F) {
         return;
      }
      if (fraction > 1.0F) {
         fraction = 1.0F;
      }
      double sweep = fraction * Math.PI * 2.0;
      for (int dx = -rInner; dx <= rInner; dx++) {
         int dyMax = (int)Math.sqrt(Math.max(0, rInner * rInner - dx * dx));
         int runStart = Integer.MIN_VALUE;
         for (int dy = -dyMax; dy <= dyMax; dy++) {
            boolean lit;
            if (fraction >= 1.0F) {
               lit = true;
            } else {
               // 角度：从正上方(0)顺时针增大
               double ang = Math.atan2(dx, -dy);
               if (ang < 0) {
                  ang += Math.PI * 2.0;
               }
               lit = ang <= sweep;
            }
            if (lit) {
               if (runStart == Integer.MIN_VALUE) {
                  runStart = dy;
               }
            } else if (runStart != Integer.MIN_VALUE) {
               g.fill(cx + dx, cy + runStart, cx + dx + 1, cy + dy, color);
               runStart = Integer.MIN_VALUE;
            }
         }
         if (runStart != Integer.MIN_VALUE) {
            g.fill(cx + dx, cy + runStart, cx + dx + 1, cy + dyMax + 1, color);
         }
      }
   }
}