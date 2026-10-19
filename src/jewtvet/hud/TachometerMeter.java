package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 电表（E-Meter）：方形面板，显示电机功率 / 电流 / 电压 / 转速。
 *
 * 电压恒定 120 V；功率与电流由电机转速和船速推算。
 * 功率峰值 150 kW，电流 = 功率 / 电压。
 */
public final class TachometerMeter {
   public static final int MAX_RPM = 6000;      // 电机最高转速
   public static final int IDLE_RPM = 0;     // 电机静止即 0
   public static final float VOLTAGE = 120.0F;  // 恒定电压
   public static final float PEAK_KW = 150.0F;  // 峰值功率

   private static final int W = 126;
   private static final int H = 68;
   private static final int PAD = 4;

   private static final int PANEL  = 0xC0101010;
   private static final int BORDER = 0xFF3A3A3A;
   private static final int LABEL  = 0xFF9A9A9A;
   private static final int VALUE  = 0xFFFFFFFF;
   private static final int ACCENT = 0xFF3FD1FF;
   private static final int WARN   = 0xFFFF7A1A;

   private TachometerMeter() {
   }

   public static int width() { return W; }
   public static int height() { return H; }

   /**
    * @param motorRpm  电机转速 0..1
    * @param speedBps  船速（格/秒）
    * @param gear      -1=R 0=N 1..6
    */
   public static void render(GuiGraphicsExtractor g, Minecraft mc, float motorRpm, double speedBps, int gear) {
      if (motorRpm < 0.0F) motorRpm = 0.0F;
      if (motorRpm > 1.0F) motorRpm = 1.0F;

      float powerKw = motorRpm * PEAK_KW * (0.55F + 0.45F * (float)Math.min(1.0, speedBps / 40.0));
      float currentA = powerKw * 1000.0F / VOLTAGE;
      int rpmValue = Math.round(motorRpm * MAX_RPM);

      // ---------- stratum 1：面板 ----------
      g.fill(0, 0, W, H, PANEL);
      g.fill(0, 0, W, 1, BORDER);
      g.fill(0, H - 1, W, H, BORDER);
      g.fill(0, 0, 1, H, BORDER);
      g.fill(W - 1, 0, W, H, BORDER);

      // 标题分隔线
      g.fill(PAD, PAD + 10, W - PAD, PAD + 11, 0x40FFFFFF);

      // 单速标识（右上）：电车没有挡位

      // 转速条（功率条直接用 rpm 比例）
      int barX = PAD;
      int barY = H - PAD - 4;
      int barW = W - PAD * 2;
      g.fill(barX, barY, barX + barW, barY + 4, 0x30FFFFFF);
      int lit = (int)(barW * motorRpm);
      g.fill(barX, barY, barX + lit, barY + 4, motorRpm > 0.85F ? WARN : ACCENT);

      // ---------- stratum 2：文字 ----------
      g.nextStratum();

      g.text(mc.font, "\u7535\u673a MOTOR", PAD, PAD, ACCENT, false);

      String drive = "SINGLE";
      int gw = mc.font.width(drive);
      g.text(mc.font, drive, W - PAD - gw, PAD, ACCENT, false);

      int y = PAD + 14;
      row(g, mc, "\u529f\u7387 Power", String.format("%.1f kW", powerKw), y, motorRpm > 0.85F ? WARN : VALUE);
      y += 12;
      row(g, mc, "\u7535\u6d41 Current", String.format("%.0f A", currentA), y, VALUE);
      y += 12;
      row(g, mc, "\u7535\u538b Voltage", String.format("%.0f V", VOLTAGE), y, 0xFF7FD4FF);
      y += 12;
      row(g, mc, "\u8f6c\u901f RPM", Integer.toString(rpmValue), y, VALUE);
   }

   private static void row(GuiGraphicsExtractor g, Minecraft mc, String label, String value, int y, int valueColor) {
      g.text(mc.font, label, PAD, y, LABEL, false);
      int vw = mc.font.width(value);
      g.text(mc.font, value, W - PAD - vw, y, valueColor, false);
   }
}
