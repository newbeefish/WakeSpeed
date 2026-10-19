package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * 漂移 / 烧胎 双计时（仿 Forza Horizon）。
 *
 * 漂移：|侧滑角| > 45°，速度 > 4 格/秒。
 * 烧胎：在漂移基础上额外要求
 *   - |侧滑角| > 60°
 *   - 持续同向（转向方向不能翻转）
 *   - 在**当前原点**的 12x12 范围内（±6 格），原点每满 1 秒滚到当前位置
 *   - 满足上述条件连续满 1 秒
 *
 * 两个计时**独立**：
 *   driftTicks   —— 只要在漂移就一直涨（方向翻转/停止才清零）
 *   burnoutTicks —— 只在烧胎条件成立时涨，条件一断立刻清零
 */
public final class DriftMeter {
   public static boolean drifting = false;
   public static int driftTicks = 0;
   public static int burnoutTicks = 0;
   public static boolean burnout = false;
   public static int direction = 0;

   private static final double MIN_ANGLE = 45.0;
   private static final double BURNOUT_MIN_ANGLE = 60.0;
   private static final double MIN_SPEED = 4.0;
   private static final double AREA_HALF = 6.0;          // 12x12 -> 半边长 6
   private static final int BURNOUT_TICKS = 20;          // 1 秒
   private static final int ANCHOR_RESET_TICKS = 20;     // 每 1 秒滚动原点

   private static double anchorX = 0.0;
   private static double anchorZ = 0.0;

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
      boolean active = Math.abs(driftAngle) > MIN_ANGLE && speed > MIN_SPEED;

      if (!active) {
         reset();
         return;
      }

      int dir = driftAngle > 0.0 ? 1 : -1;

      if (!drifting || dir != direction) {
         // 刚开始，或转向反了 -> 两个计时都重新开始
         drifting = true;
         driftTicks = 0;
         burnoutTicks = 0;
         burnout = false;
         anchorX = x;
         anchorZ = z;
         direction = dir;
      }

      driftTicks++;

      double dx = Math.abs(x - anchorX);
      double dz = Math.abs(z - anchorZ);
      boolean inArea = dx <= AREA_HALF && dz <= AREA_HALF;
      boolean hot = inArea && Math.abs(driftAngle) > BURNOUT_MIN_ANGLE;

      // 烧胎计时：条件成立才涨，否则立刻清零
      if (hot) {
         burnoutTicks++;
      } else {
         burnoutTicks = 0;
      }
      burnout = burnoutTicks >= BURNOUT_TICKS;

      // 判定完再滚动原点
      if (driftTicks % ANCHOR_RESET_TICKS == 0) {
         anchorX = x;
         anchorZ = z;
      }
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
      String label = burnout ? "\u70e7\u80ce BURNOUT" : "DRIFT";
      int labelColor = burnout ? 0xFFFF7A1A : 0xFF7FD4FF;
      int timeColor = burnout ? 0xFFFFB347 : 0xFFFFFFFF;

      Matrix3x2fStack m = g.pose();

      int lw = mc.font.width(label);
      g.text(mc.font, label, cx - lw / 2, baseY - 12, labelColor, true);

      // 漂移总时长（大号）
      m.pushMatrix();
      m.translate((float)cx, (float)(baseY + 2));
      m.scale(2.0F, 2.0F);
      int tw = mc.font.width(driftTime);
      g.text(mc.font, driftTime, -tw / 2, 0, timeColor, true);
      m.popMatrix();

      // 烧胎独立计时（条件成立时才显示）
      if (burnoutTicks > 0) {
         String bt = "\u70e7\u80ce " + String.format("%.2f", burnoutSeconds());
         int bw = mc.font.width(bt);
         g.text(mc.font, bt, cx - bw / 2, baseY + 15, burnout ? 0xFFFF7A1A : 0xFFFFC08A, true);
      }

      // 进度条：烧胎计时优先，否则显示漂移到 1 秒的进度
      float frac = burnoutTicks > 0
         ? Math.min(1.0F, burnoutSeconds() / 1.0F)
         : Math.min(1.0F, driftSeconds() / 1.0F);
      int barW = 60;
      int barX = cx - barW / 2;
      int barY = baseY + 26;
      g.fill(barX - 1, barY - 1, barX + barW + 1, barY + 3, 0x90000000);
      int fillW = (int)(barW * frac);
      int barColor = burnoutTicks > 0 ? 0xFFFF7A1A : 0xFF7FD4FF;
      g.fill(barX, barY, barX + fillW, barY + 2, barColor);
   }
}
