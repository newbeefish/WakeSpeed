package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 条状转速表（W2RC / 拉力赛车风格）+ 下方涡轮条。
 *
 * 上面一行：18 段 LED 转速条（绿/黄/红）+ 转速数字 + 挡位框
 * 下面一行：白色涡轮条（拉满变淡蓝）+ 涡轮转速文字（EV 模式整行隐藏）
 *
 * 26.x 的 GUI 是延迟批处理：填充和文字必须分 stratum，否则文字会被盖住。
 */
public final class TachometerBar {
   public static final int MAX_RPM = 8000;
   public static final int IDLE_RPM = 800;
   /** 涡轮满转时的显示转速（真实涡轮可达十几万转） */
   public static final int MAX_TURBO_RPM = 180000;

   // 尺寸（像素）
   private static final int SEGMENTS = 18;
   private static final int SEG_W = 5;
   private static final int SEG_GAP = 1;
   private static final int SEG_H = 10;
   private static final int PAD = 3;

   /** 涡轮条高度与行间距 */
   private static final int TURBO_H = 4;
   private static final int TURBO_GAP = 2;

   // 右侧区域
   private static final int RPM_SLOT_W = 22;
   private static final int RPM_GAP = 4;
   private static final int GEAR_W = 13;
   private static final int GEAR_H = 10;

   // 颜色
   private static final int GREEN  = 0xFF37D14A;
   private static final int YELLOW = 0xFFF2C41E;
   private static final int RED    = 0xFFE02424;
   private static final int SLOT   = 0x33FFFFFF;
   private static final int PANEL  = 0xB0101010;
   private static final int BORDER = 0xFF3A3A3A;
   private static final int GEARBG = 0xFF1A1A1A;
   private static final int TEXT_HI = 0xFFFFFFFF;
   private static final int TEXT_DIM = 0xFF9A9A9A;
   private static final int TURBO_FILL = 0xFFF2F2F2;
   private static final int TURBO_SLOT = 0x30FFFFFF;
   private static final int TURBO_FULL = 0xFF6FE3FF;

   private static final int YELLOW_FROM = 5500;
   private static final int RED_FROM    = 7000;

   private TachometerBar() {
   }

   private static int colorForRpm(int rpm) {
      if (rpm >= RED_FROM) return RED;
      if (rpm >= YELLOW_FROM) return YELLOW;
      return GREEN;
   }

   public static int barWidth() {
      return SEGMENTS * (SEG_W + SEG_GAP) - SEG_GAP;
   }

   public static int width() {
      return barWidth() + PAD * 2 + RPM_SLOT_W + RPM_GAP + GEAR_W;
   }

   /** 最高高度（含涡轮行）；EV 模式实际只画上面一行 */
   public static int height() {
      return SEG_H + PAD * 2 + TURBO_GAP + TURBO_H + 4;
   }

   /**
    * @param rpmValue  实际转速 0..8000
    * @param gear      -1=R, 0=N, 1..6
    * @param turbo     涡轮转速 0..1
    * @param showTurbo 是否显示涡轮行（EV 模式传 false）
    */
   public static void render(GuiGraphicsExtractor g, Minecraft mc, float rpmValue, int gear,
                             float turbo, boolean showTurbo) {
      if (rpmValue < 0.0F) rpmValue = 0.0F;
      if (rpmValue > MAX_RPM) rpmValue = MAX_RPM;
      if (turbo < 0.0F) turbo = 0.0F;
      if (turbo > 1.0F) turbo = 1.0F;

      int barW = barWidth();
      int panelW = width();
      int panelH = showTurbo ? height() : (SEG_H + PAD * 2);

      int rpmX = barW + PAD + PAD / 2;
      int gearX = rpmX + RPM_SLOT_W + RPM_GAP;
      int gearY = (SEG_H - GEAR_H) / 2;
      int turboY = PAD + SEG_H + TURBO_GAP;

      // ---------- stratum 1：所有填充 ----------
      g.fill(0, 0, panelW, panelH, PANEL);
      g.fill(0, 0, panelW, 1, BORDER);
      g.fill(0, panelH - 1, panelW, panelH, BORDER);
      g.fill(0, 0, 1, panelH, BORDER);
      g.fill(panelW - 1, 0, panelW, panelH, BORDER);

      // 转速 LED 分段
      int lit = (int)Math.round(rpmValue / (double)MAX_RPM * SEGMENTS);
      int redlineSlot = (int)Math.floor(RED_FROM / (double)MAX_RPM * SEGMENTS);
      int segY = PAD;
      for (int i = 0; i < SEGMENTS; i++) {
         int sx = PAD + i * (SEG_W + SEG_GAP);
         int midRpm = (int)((i + 0.5) / SEGMENTS * MAX_RPM);
         if (i < lit) {
            g.fill(sx, segY, sx + SEG_W, segY + SEG_H, colorForRpm(midRpm));
         } else {
            int slot = i >= redlineSlot ? 0x55E02424 : SLOT;
            g.fill(sx, segY, sx + SEG_W, segY + SEG_H, slot);
         }
      }

      // 挡位方框
      g.fill(gearX, PAD + gearY, gearX + GEAR_W, PAD + gearY + GEAR_H, GEARBG);
      g.fill(gearX, PAD + gearY, gearX + GEAR_W, PAD + gearY + 1, BORDER);
      g.fill(gearX, PAD + gearY + GEAR_H - 1, gearX + GEAR_W, PAD + gearY + GEAR_H, BORDER);
      g.fill(gearX, PAD + gearY, gearX + 1, PAD + gearY + GEAR_H, BORDER);
      g.fill(gearX + GEAR_W - 1, PAD + gearY, gearX + GEAR_W, PAD + gearY + GEAR_H, BORDER);

      // 涡轮条（白色，EV 模式不显示）
      if (showTurbo) {
         int tbX = PAD;
         int tbW = barW - 34;
         g.fill(tbX, turboY, tbX + tbW, turboY + TURBO_H, TURBO_SLOT);
         int tbLit = (int)Math.round(tbW * turbo);
         if (tbLit > 0) {
            g.fill(tbX, turboY, tbX + tbLit, turboY + TURBO_H,
               turbo >= 0.995F ? TURBO_FULL : TURBO_FILL);
         }
      }

      // ---------- stratum 2：文字 ----------
      g.nextStratum();

      String rpmText = Integer.toString((int)Math.round(rpmValue));
      g.text(mc.font, rpmText, rpmX, PAD + 1, rpmValue >= RED_FROM ? RED : TEXT_HI, false);

      String gearText = gear < 0 ? "R" : (gear == 0 ? "N" : Integer.toString(gear));
      int gearColor = gear >= 6 ? RED : (gear < 0 ? 0xFFFF9A3C : (gear == 0 ? TEXT_DIM : TEXT_HI));
      int gw = mc.font.width(gearText);
      g.text(mc.font, gearText, gearX + (GEAR_W - gw) / 2, PAD + gearY + 1, gearColor, false);

      if (showTurbo) {
         String tText = turboRpmText(turbo);
         int tw = mc.font.width(tText);
         // 涡轮文字放在涡轮条右侧、单独一行（垂直居中于涡轮行）
         g.text(mc.font, tText, panelW - PAD - tw, turboY + 1,
            turbo >= 0.995F ? TURBO_FULL : TEXT_DIM, false);
      }
   }

   /** 涡轮转速文案：0 -> "T 0"，满 -> "T 180k" */
   private static String turboRpmText(float turbo) {
      int rpm = Math.round(turbo * MAX_TURBO_RPM);
      if (rpm >= 1000) {
         return "T " + (rpm / 1000) + "k";
      }
      return "T " + rpm;
   }
}
