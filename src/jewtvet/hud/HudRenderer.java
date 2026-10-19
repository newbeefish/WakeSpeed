package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.joml.Matrix3x2fStack;

public class HudRenderer {
   private static final Identifier WIDGETS = Identifier.fromNamespaceAndPath("wakespeed", "textures/boathud.png");
   private static final Identifier TIME_TEX = Identifier.fromNamespaceAndPath("wakespeed", "textures/boathud2.png");
   private static final int TEX = 256;

   private final Minecraft client;
   private double displayedSpeed = 0.0;
   private double displayedDrift = 0.0;
   /** 转速表指针的平滑值（消除 1px 指针的舍入抖动） */
   /** 是否电车模式（EV 模式下不显示涡轮条） */
   private static boolean evSensor() {
      return EngineLink.readEvMode();
   }

   private float displayedRpm = 0.0F;
   public static double publicSpeed = 0.0;
   public static float scale = 0.45F;

   private final int[][] ice_textures = new int[][]{

      {15, 87, 120, 238},
      {16, 88, 121, 239},
      {17, 89, 122, 240},
      {18, 90, 123, 241},
      {19, 91, 124, 242},
      {20, 92, 125, 243},
      {21, 93, 126, 244},
      {22, 93, 127, 244},
      {23, 94, 128, 245},
      {24, 95, 129, 246},
      {25, 95, 130, 246},
      {26, 96, 131, 247},
      {27, 97, 132, 248},
      {28, 97, 133, 248},
      {29, 98, 134, 249},
      {30, 98, 135, 249},
      {31, 99, 136, 250},
      {32, 99, 137, 250},
      {33, 99, 138, 250},
      {34, 100, 139, 251},
      {35, 100, 140, 251},
      {36, 101, 141, 252},
      {37, 101, 142, 252},
      {38, 101, 143, 252},
      {39, 101, 144, 252},
      {40, 102, 145, 253},
      {41, 102, 146, 253},
      {42, 102, 147, 253},
      {43, 102, 148, 253},
      {44, 102, 149, 253},
      {45, 103, 150, 254},
      {46, 103, 151, 254},
      {47, 103, 152, 254},
      {48, 103, 153, 254},
      {49, 103, 154, 254},
      {50, 103, 155, 254},
      {51, 103, 156, 254},
      {52, 103, 157, 254},
      {53, 103, 158, 254},
      {54, 103, 159, 254},
      {55, 103, 160, 254},
      {56, 103, 161, 254},
      {57, 103, 162, 254},
      {58, 102, 163, 253},
      {59, 102, 164, 253},
      {60, 102, 165, 253},
      {61, 102, 166, 253},
      {62, 102, 167, 253},
      {63, 101, 168, 252},
      {64, 101, 169, 252},
      {65, 101, 170, 252},
      {66, 101, 171, 252},
      {67, 100, 172, 251},
      {68, 100, 173, 251},
      {69, 99, 174, 250},
      {70, 99, 175, 250},
      {71, 99, 176, 250},
      {72, 98, 177, 249},
      {73, 98, 178, 249},
      {74, 97, 179, 248},
      {75, 97, 180, 248},
      {76, 96, 181, 247},
      {77, 96, 182, 247},
      {78, 95, 183, 246},
      {79, 94, 184, 245},
      {80, 93, 185, 244},
      {81, 93, 186, 244},
      {82, 92, 187, 243},
      {83, 92, 188, 243},
      {84, 91, 189, 242},
      {85, 90, 190, 241},
      {86, 89, 191, 240},
      {87, 88, 192, 239},
      {88, 87, 193, 238},
      {89, 86, 194, 237},
      {90, 85, 195, 236},
      {91, 84, 196, 235},
      {92, 83, 197, 234},
      {93, 82, 198, 233},
      {93, 81, 198, 232},
      {94, 80, 199, 231},
      {95, 79, 200, 230},
      {95, 78, 200, 229},
      {96, 77, 201, 228},
      {97, 76, 202, 227},
      {97, 75, 202, 226},
      {98, 74, 203, 225},
      {98, 73, 203, 224},
      {99, 72, 204, 223},
      {99, 71, 204, 222},
      {99, 70, 204, 221},
      {100, 69, 205, 220},
      {100, 68, 205, 219},
      {101, 67, 206, 218},
      {101, 66, 206, 217},
      {101, 65, 206, 216},
      {101, 64, 206, 215},
      {102, 63, 207, 214},
      {102, 62, 207, 213},
      {102, 61, 207, 212},
      {102, 60, 207, 211},
      {102, 59, 207, 210},
      {103, 58, 208, 209},
      {103, 57, 208, 208},
      {103, 56, 208, 207},
      {103, 55, 208, 206},
      {103, 54, 208, 205},
      {103, 53, 208, 204},
      {103, 54, 208, 205},
      {103, 52, 208, 203},
      {103, 51, 208, 202},
      {103, 50, 208, 201},
      {103, 49, 208, 200},
      {103, 48, 208, 199},
      {103, 47, 208, 198},
      {103, 46, 208, 197},
      {102, 45, 207, 196},
      {102, 44, 207, 195},
      {102, 43, 207, 194},
      {102, 42, 207, 193},
      {102, 41, 207, 192},
      {101, 40, 206, 191},
      {101, 39, 206, 190},
      {101, 38, 206, 189}
   };

   private final int[][] time_textures = new int[][]{

      {11, 83, 11, 83},
      {10, 82, 10, 82},
      {10, 81, 10, 81},
      {9, 80, 9, 80},
      {8, 79, 8, 79},
      {8, 78, 8, 78},
      {7, 77, 7, 77},
      {6, 76, 6, 76},
      {6, 75, 6, 75},
      {5, 74, 5, 74},
      {5, 73, 5, 73},
      {4, 72, 4, 72},
      {4, 71, 4, 71},
      {4, 70, 4, 70},
      {3, 69, 3, 69},
      {3, 68, 3, 68},
      {2, 67, 2, 67},
      {2, 66, 2, 66},
      {2, 65, 2, 65},
      {2, 64, 2, 64},
      {1, 63, 1, 63},
      {1, 62, 1, 62},
      {1, 61, 1, 61},
      {1, 60, 1, 60},
      {1, 59, 1, 59},
      {0, 58, 0, 58},
      {0, 57, 0, 57},
      {0, 56, 0, 56},
      {0, 55, 0, 55},
      {0, 54, 0, 54},
      {0, 53, 0, 53},
      {0, 52, 0, 52},
      {0, 51, 0, 51},
      {0, 50, 0, 50},
      {0, 49, 0, 49},
      {0, 48, 0, 48},
      {0, 47, 0, 47},
      {0, 46, 0, 46},
      {1, 45, 1, 45},
      {1, 44, 1, 44},
      {1, 43, 1, 43},
      {1, 42, 1, 42},
      {1, 41, 1, 41},
      {2, 40, 2, 40},
      {2, 39, 2, 39},
      {2, 38, 2, 38},
      {2, 37, 2, 37},
      {3, 36, 3, 36},
      {3, 35, 3, 35},
      {4, 34, 4, 34},
      {4, 33, 4, 33},
      {4, 32, 4, 32},
      {5, 31, 5, 31},
      {5, 30, 5, 30},
      {6, 29, 6, 29},
      {6, 28, 6, 28},
      {7, 27, 7, 27},
      {8, 26, 8, 26},
      {8, 25, 8, 25},
      {9, 24, 9, 24},
      {9, 23, 9, 23},
      {10, 22, 10, 22},
      {10, 21, 10, 21},
      {11, 20, 11, 20},
      {12, 19, 12, 19},
      {13, 18, 13, 18},
      {14, 17, 14, 17},
      {15, 16, 15, 16},
      {16, 15, 16, 15},
      {17, 14, 17, 14},
      {18, 13, 18, 13},
      {19, 12, 19, 12},
      {20, 11, 20, 11},
      {21, 10, 21, 10},
      {22, 10, 22, 10},
      {23, 9, 23, 9},
      {24, 8, 24, 8},
      {25, 8, 25, 8},
      {26, 7, 26, 7},
      {27, 6, 27, 6},
      {28, 6, 28, 6},
      {29, 5, 29, 5},
      {30, 5, 30, 5},
      {31, 4, 31, 4},
      {32, 4, 32, 4},
      {33, 4, 33, 4},
      {34, 3, 34, 3},
      {35, 3, 35, 3},
      {36, 2, 36, 2},
      {37, 2, 37, 2},
      {38, 2, 38, 2},
      {39, 2, 39, 2},
      {40, 1, 40, 1},
      {41, 1, 41, 1},
      {42, 1, 42, 1},
      {43, 1, 43, 1},
      {44, 1, 44, 1},
      {45, 0, 45, 0},
      {46, 0, 46, 0},
      {47, 0, 47, 0},
      {48, 0, 48, 0},
      {49, 0, 49, 0},
      {50, 0, 50, 0},
      {51, 0, 51, 0},
      {52, 0, 52, 0},
      {53, 0, 53, 0},
      {54, 0, 54, 0},
      {55, 0, 55, 0},
      {56, 0, 56, 0},
      {57, 0, 57, 0},
      {58, 1, 58, 1},
      {59, 1, 59, 1},
      {60, 1, 60, 1},
      {61, 1, 61, 1},
      {62, 1, 62, 1},
      {63, 2, 63, 2},
      {64, 2, 64, 2},
      {65, 2, 65, 2},
      {66, 2, 66, 2},
      {67, 3, 67, 3},
      {68, 3, 68, 3},
      {69, 4, 69, 4},
      {70, 4, 70, 4},
      {71, 4, 71, 4},
      {72, 5, 72, 5},
      {73, 5, 73, 5},
      {74, 6, 74, 6},
      {75, 6, 75, 6},
      {76, 7, 76, 7},
      {77, 8, 77, 8},
      {78, 8, 78, 8},
      {79, 9, 79, 9},
      {80, 10, 80, 10},
      {81, 10, 81, 10},
      {82, 10, 82, 10},
      {83, 11, 83, 11},
      {84, 12, 84, 12},
      {85, 13, 85, 13},
      {86, 14, 86, 14},
      {87, 15, 87, 15},
      {88, 16, 88, 16},
      {89, 17, 89, 17},
      {90, 18, 90, 18},
      {91, 19, 91, 19},
      {92, 20, 92, 20},
      {93, 21, 93, 21},
      {93, 22, 93, 22},
      {94, 23, 94, 23},
      {94, 24, 94, 24},
      {95, 25, 95, 25},
      {96, 26, 96, 26},
      {96, 27, 96, 27},
      {97, 28, 97, 28},
      {97, 29, 97, 29},
      {98, 30, 98, 30},
      {98, 31, 98, 31},
      {99, 32, 99, 32},
      {99, 33, 99, 33}
   };

   public HudRenderer(Minecraft client) {
      this.client = client;
   }

   public void render(GuiGraphicsExtractor g, float tickDelta) {
      if (Common.CONFIG == null || Common.CONFIG.mod_enabled != ConfigEnums.mod_enabled.Yes) {
         return;
      }
      if (Common.hudData == null) {
         return;
      }
      if (Common.CONFIG.resize_map == ConfigEnums.resize_map.No) {
         scale = 0.45F;
      } else {
         scale = Math.max(0.45F, (float)(1.0 - 0.015F * publicSpeed));
      }

      int sw = g.guiWidth();
      int sh = g.guiHeight();

      this.displayedSpeed = Mth.lerp(tickDelta, (float)this.displayedSpeed, (float)Common.hudData.speed);
      publicSpeed = this.displayedSpeed;
      this.displayedDrift = Mth.lerp(tickDelta, (float)this.displayedDrift, (float)Common.hudData.driftAngle);
      // 目标转速值（0..8000，与音效模组 rpm 线性对应）
      float targetRpm = Tachometer.MAX_RPM * Common.hudData.rpm;
      boolean revInput = this.client.options.keyDown.isDown()
                      && !this.client.options.keyUp.isDown();

      boolean evMode = EngineLink.readEvMode()
                      || Common.CONFIG.rpm_jitter == ConfigEnums.rpm_jitter.Off;

      if (evMode) {
         // 电车：PID 闭环控制，转速几乎不抖（±2 转以内）
         targetRpm += (float)(Math.sin(System.nanoTime() / 1.0e9 * 6.0) * 2.0);
      } else if (Common.hudData.throttle && Common.hudData.rpm >= 0.99F) {
         // 前进到顶断油：指针快速下坠抖动
         double sec = System.nanoTime() / 1.0e9;
         float wob = (float)(Math.abs(Math.sin(sec * 23.0)) * 380.0);
         targetRpm = Tachometer.MAX_RPM - wob;
      } else if (revInput && Common.hudData.rpm >= 0.47F) {
         // 倒档限速（上限 4000 转）：在 4000 附近抖动
         double sec = System.nanoTime() / 1.0e9;
         float wob = (float)(Math.abs(Math.sin(sec * 21.0)) * 210.0);
         targetRpm = Tachometer.MAX_RPM * 0.5F - wob;
      } else if (Common.hudData.rpm <= 0.16F) {
         // 怠速：指针轻微游动
         double sec = System.nanoTime() / 1.0e9;
         targetRpm += (float)(Math.sin(sec * 9.1) * 16.0 + Math.sin(sec * 17.3) * 8.0);
      }
      this.displayedRpm = Mth.lerp(0.55F, this.displayedRpm, targetRpm);

      if (this.displayedSpeed > 80.0) this.displayedSpeed = 80.0;
      if (this.displayedSpeed < 0.0) this.displayedSpeed = 0.0;
      if (this.displayedDrift > 180.0) this.displayedDrift = 180.0;
      if (this.displayedDrift < -180.0) this.displayedDrift = -180.0;

      if (Common.CONFIG.render_drift == ConfigEnums.render_drift.Yes) {
         DriftMeter.render(g, this.client);
      }

      if (Common.CONFIG.render_rpm == ConfigEnums.render_rpm.Yes) {
         int rsz = 100;
         int rxs = sw - rsz - 20;
         int rys = sh - rsz - 20;
         int rox = (int)Math.round(Common.CONFIG.rpm_x / 100.0 * rxs) + 10;
         int roy = (int)Math.round(Common.CONFIG.rpm_y / 100.0 * rys) + 10;
         Matrix3x2fStack rm = g.pose();
         rm.pushMatrix();
         if (Common.CONFIG.rpm_style == ConfigEnums.rpm_style.Dial) {
            rm.translate((float)rox, (float)roy);
            Tachometer.render(g, this.client, this.displayedRpm, Common.hudData.gear, Common.hudData.turbo, !evSensor());
         } else if (Common.CONFIG.rpm_style == ConfigEnums.rpm_style.Meter) {
            // 电表：方形面板（功率/电流/电压/转速）
            rm.translate((float)(rox + 6), (float)(roy + 6));
            TachometerMeter.render(g, this.client,
               Common.hudData.rpm, Common.hudData.speed, Common.hudData.gear);
         } else {
            // 条状表：左上角稍作内缩
            rm.translate((float)(rox + 6), (float)(roy + 6));
            TachometerBar.render(g, this.client, this.displayedRpm, Common.hudData.gear, Common.hudData.turbo, !evSensor());
         }
         rm.popMatrix();
      }
      if (Common.CONFIG.render_speed == ConfigEnums.render_speed.Yes) {
         int size = 99;
         int screenX = sw - size - 26;
         int screenY = sh - size - 26;
         int ox = (int)Math.round(Common.CONFIG.speed_x / 100.0 * screenX) + 13;
         int oy = (int)Math.round(Common.CONFIG.speed_y / 100.0 * screenY) + 13;
         this.renderSpeedometer(g, ox, oy);
      }

      if (Common.CONFIG.render_map == ConfigEnums.render_map.Yes) {
         MapHelper.ensureInit(this.client);
         if (MapHelper.needsUpdate(this.client)) {
            MapHelper.rebuild(this.client);
         }
         MapHelper.updateVisual(this.client);   // 每帧按玩家位置重烘圆形蒙版
         int msize = 99;
         int msx = sw - msize - 20;
         int msy = sh - msize - 20;
         int mox = (int)Math.round(Common.CONFIG.map_x / 100.0 * msx) + 10;
         int moy = (int)Math.round(Common.CONFIG.map_y / 100.0 * msy) + 10;
         this.renderMinimap(g, mox, moy);
      }
      if (Common.CONFIG.render_race_status == ConfigEnums.render_race_status.Yes) {
         int ssw = sw - 120 - 20;
         int ssh = sh - 31 - 20;
         int ox = (int)Math.round(Common.CONFIG.status_x / 100.0 * ssw) + 10;
         int oy = (int)Math.round(Common.CONFIG.status_y / 100.0 * ssh) + 10;
         this.renderRaceStatus(g, new int[]{ox, oy, 101, 198, 120, 31});
      }
   }

   private void blit(GuiGraphicsExtractor g, Identifier tex, int x, int y, int u, int v, int w, int h) {
      g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float)u, (float)v, w, h, TEX, TEX);
   }

   private void renderSpeedometer(GuiGraphicsExtractor g, int bx, int by) {
      // ice speed ring
      this.renderIce(g, this.displayedSpeed, bx, by);
      // lap segment ring
      this.renderTrackTimes(g, bx, by);
      // dial face
      this.blit(g, WIDGETS, bx, by, 0, 0, 99, 99);
      // speed digits
      this.renderSpeedDigits(g, this.displayedSpeed, bx, by);

      float speedAngle = (float)Math.toRadians(this.displayedSpeed * 2.78125 - 111.25);
      float driftAngle = (float)Math.toRadians(Math.abs(this.displayedDrift) * 0.2847222F)
                       * (this.displayedDrift < 0.0 ? 1.0F : -1.0F);
      Matrix3x2fStack m = g.pose();
      double cx = bx + 49.5;
      double cy = by + 49.5;
      m.pushMatrix();
      m.translate((float)cx, (float)cy);
      m.rotate(driftAngle);
      m.translate((float)(-cx), (float)(-cy));
      this.blit(g, WIDGETS, (int)(cx - 1.5), by + 91, 205,
                this.displayedDrift < -3.0 ? 14 : (this.displayedDrift > 3.0 ? 7 : 0), 4, 7);
      m.popMatrix();
      m.pushMatrix();
      m.translate((float)cx, (float)cy);
      m.rotate(speedAngle);
      m.translate((float)(-cx), (float)(-cy));
      int pu = this.displayedSpeed < 39.0 ? 220 : 221;
      int pv = this.displayedSpeed < 39.0 ? 50 : (this.displayedSpeed > 41.0 ? 100 : 0);
      this.blit(g, WIDGETS, (int)(cx - 1.5), by + 5, pu, pv, 4, 50);
      m.popMatrix();
      // foreground ring
      this.blit(g, WIDGETS, bx, by, 99, 0, 99, 99);
   }

   private void renderIce(GuiGraphicsExtractor g, double speed, int bx, int by) {
      int rounded = (int)Math.round(speed * 1.7857);
      int max = Math.min(rounded, this.ice_textures.length);
      for (int i = 0; i < max; i++) {
         this.blit(g, WIDGETS, bx - 3 + this.ice_textures[i][0], by - 3 + this.ice_textures[i][1],
                   this.ice_textures[i][2], this.ice_textures[i][3], 2, 2);
      }
   }

   private void renderTrackTimes(GuiGraphicsExtractor g, int bx, int by) {
      java.util.ArrayList<Integer> list = new java.util.ArrayList<>(Common.hudData.track_times);
      if (list.isEmpty()) {
         this.blit(g, WIDGETS, bx - 3, by - 3, 105, 99, 101, 85);
         return;
      }
      int qty = list.size();
      double len = (double)this.time_textures.length / qty;
      int each = (int)Math.floor(len);
      int covered = 0;
      for (int i = 0; i < qty; i++) {
         int status = list.get(i);
         int start = i * each;
         for (int k = 0; k < each; k++) {
            covered++;
            int idx = start + k;
            this.blit(g, TIME_TEX, bx - 3 + this.time_textures[idx][0], by - 3 + this.time_textures[idx][1],
                      this.time_textures[idx][2], this.time_textures[idx][3] + 85 * status, 2, 2);
         }
      }
      for (int i = covered; i < this.time_textures.length; i++) {
         int status = list.get(qty - 1);
         this.blit(g, TIME_TEX, bx - 3 + this.time_textures[i][0], by - 3 + this.time_textures[i][1],
                   this.time_textures[i][2], this.time_textures[i][3] + 85 * status, 2, 2);
      }
      list.clear();
   }

   private void renderMinimap(GuiGraphicsExtractor g, int bx, int by) {
      if (!MapHelper.ready || this.client.player == null) {
         return;
      }
      double cx = bx + 49.5;
      double cy = by + 49.5;
      // circular background ring
      this.blit(g, TIME_TEX, bx, by, 101, 0, 99, 99);

      // scale so that the rounded disc (diameter SIZE*0.94) exactly fits in 99px
      float ts = (float)(99.0 * 0.94 / (MapHelper.SIZE * 0.94));

      // player offset from the map texture origin, in blocks
      double px = this.client.player.getX() - MapHelper.mapBlockPos.getX();
      double pz = this.client.player.getZ() - MapHelper.mapBlockPos.getZ();

      // same convention as the original mod: facing direction points up
      float rot = (float)Math.toRadians(-this.client.player.getYRot() + 180.0F);

      Matrix3x2fStack m = g.pose();
      m.pushMatrix();
      m.translate((float)cx, (float)cy);
      m.rotate(rot);
      m.scale(ts, ts);
      m.translate((float)(-px), (float)(-pz));
      g.blit(RenderPipelines.GUI_TEXTURED, MapHelper.TEX_ID,
             0, 0, 0.0F, 0.0F,
             MapHelper.SIZE, MapHelper.SIZE, MapHelper.SIZE, MapHelper.SIZE);
      m.popMatrix();

      // player arrow
      this.blit(g, TIME_TEX, bx + 47, by + 47, 200, 0, 5, 6);
   }
   private void renderSpeedDigits(GuiGraphicsExtractor g, double speed, int bx, int by) {
      int kmh = (int)Math.round(speed * 3.6F);
      String s = Integer.toString(kmh);
      for (int i = 0; i < s.length(); i++) {
         int digit = s.charAt(s.length() - 1 - i) - '0';
         this.blit(g, WIDGETS, bx + 53 - 6 * i, by + 65, 210, 9 * digit, 5, 9);
      }
   }

   private void renderRaceStatus(GuiGraphicsExtractor g, int[] bg) {
      // panel background
      this.blit(g, TIME_TEX, bg[0], bg[1], bg[2], bg[3], bg[4], bg[5]);
      // 26.x 延迟批处理：文字必须在新 stratum 才能画在面板之上
      g.nextStratum();

      // position (2x)
      Matrix3x2fStack m = g.pose();
      m.pushMatrix();
      m.translate((float)(bg[0] + 19.5), (float)(bg[1] + 14.5));
      m.scale(2.0F, 2.0F);
      String pos = Common.hudData.race_current_position;
      String posText = (pos == null || pos.isEmpty()) ? "-" : pos;
      int off = posText.length() == 1 ? -3 : (posText.length() == 2 ? -6 : -3);
      g.text(this.client.font, posText, off, -3, 0xFFBC9262, false);
      m.popMatrix();

      String upper;
      String lower;
      if (Common.hudData.isRaceType) {
         upper = Common.hudData.race_current_pits;
         lower = Common.hudData.race_current_laps;
         g.text(this.client.font, "PITS", bg[0] + 39, bg[1] + 4, 0xFFBC9262, false);
         g.text(this.client.font, "LAPS", bg[0] + 39, bg[1] + 20, 0xFFFFFFFF, false);
      } else {
         upper = Common.hudData.time_current_lap;
         lower = Common.hudData.time_prev_lap;
         g.text(this.client.font, "TIME", bg[0] + 39, bg[1] + 4, 0xFFBC9262, false);
         g.text(this.client.font, "PREV", bg[0] + 39, bg[1] + 20, 0xFFFFFFFF, false);
      }
      if (upper == null) upper = "-";
      if (lower == null) lower = "-";
      g.text(this.client.font, upper, bg[0] + 115 - this.calcOffset(upper), bg[1] + 4, 0xFFBC9262, false);
      g.text(this.client.font, lower, bg[0] + 115 - this.calcOffset(lower), bg[1] + 20, 0xFFFFFFFF, false);
   }

   private int calcOffset(String text) {
      int total = text.length();
      int special = 0;
      for (char c : text.toCharArray()) {
         if (c == ',' || c == '.' || c == ':') special++;
      }
      int full = total - special;
      return full * 5 + special + (total - 1);
   }
}
