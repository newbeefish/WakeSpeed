package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * 漂移 / 烧胎计时（极简版）。
 *
 * 漂移：|侧滑角| > 45° 且 速度 > 4 格/秒
 * 烧胎：漂移中 |侧滑角| > 60° 连续满 1 秒
 */
public final class DriftMeter {
   public static boolean drifting = false;
   public static int driftTicks = 0;
   public static int burnoutTicks = 0;
   public static boolean burnout = false;
   public static int direction = 0;

   private static final double MIN_ANGLE = 45.0;
   private static final double BURNOUT_ANGLE = 60.0;
   private static final double MIN_SPEED = 2.0;
   private static final int BURNOUT_TICKS = 20;

   private DriftMeter() {
   }

   public static void reset() {
      drifting = false;
      driftTicks = 0;
      burnoutTicks = 0;
      burnout = false;
      direction = 0;
   }

   public static void tick(double x, double z, double speed, double driftAngle) {
      double ang = Math.abs(driftAngle);

      if (ang <= MIN_ANGLE || speed <= MIN_SPEED) {
         reset();
         return;
      }

      drifting = true;
      driftTicks++;
      direction = driftAngle > 0.0 ? 1 : -1;

      if (ang > BURNOUT_ANGLE) {
         burnoutTicks++;
      } else {
         burnoutTicks = 0;
      }
      burnout = burnoutTicks >= BURNOUT_TICKS;
   }

   public static float driftSeconds() {
      return driftTicks / 20.0F;
   }

   public static float burnoutSeconds() {
      return burnoutTicks / 20.0F;
   }

   public static void render(GuiGraphicsExtractor g, Minecraft mc) {
      if (!drifting) {
         return;
      }
      int cx = g.guiWidth() / 2;
      int baseY = g.guiHeight() - 50;

      String driftTime = String.format("%.2f", driftSeconds());
      String label = burnout ? "烧胎 BURNOUT" : "DRIFT";
      int labelColor = burnout ? 0xFFFF7A1A : 0xFF7FD4FF;
      int timeColor = burnout ? 0xFFFFB347 : 0xFFFFFFFF;

      Matrix3x2fStack m = g.pose();

      int lw = mc.font.width(label);
      g.text(mc.font, label, cx - lw / 2, baseY - 12, labelColor, true);

      m.pushMatrix();
      m.translate((float)cx, (float)(baseY + 2));
      m.scale(2.0F, 2.0F);
      int tw = mc.font.width(driftTime);
      g.text(mc.font, driftTime, -tw / 2, 0, timeColor, true);
      m.popMatrix();

      if (burnoutTicks > 0) {
         String bt = "烧胎 " + String.format("%.2f", burnoutSeconds());
         int bw = mc.font.width(bt);
         g.text(mc.font, bt, cx - bw / 2, baseY + 15, burnout ? 0xFFFF7A1A : 0xFFFFC08A, true);
      }

      float frac = burnoutTicks > 0
         ? Math.min(1.0F, burnoutSeconds())
         : Math.min(1.0F, driftSeconds());
      int barW = 60;
      int barX = cx - barW / 2;
      int barY = baseY + 26;
      g.fill(barX - 1, barY - 1, barX + barW + 1, barY + 3, 0x90000000);
      g.fill(barX, barY, barX + (int)(barW * frac), barY + 2,
         burnoutTicks > 0 ? 0xFFFF7A1A : 0xFF7FD4FF);
   }
}