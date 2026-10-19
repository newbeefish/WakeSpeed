package jewtvet.hud;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 小地图：把玩家周围冰面采样成 NativeImage，注册为动态贴图。
 * 含上下 map_depth 层深度搜索（越深越淡），只在换区块/Y 变化时重建。
 */
public final class MapHelper {
   public static final Identifier TEX_ID = Identifier.fromNamespaceAndPath("wakespeed", "textures/minimap");
   public static final int CHUNK_RADIUS = 7;
   public static final int SIZE = (CHUNK_RADIUS * 2 + 1) * 16;   // 240

   public static NativeImage image;
   public static NativeImage rounded;
   public static DynamicTexture texture;
   public static boolean ready = false;

   public static BlockPos mapBlockPos = BlockPos.ZERO;
   private static ChunkPos lastChunk = null;
   private static int lastY = Integer.MIN_VALUE;

   private MapHelper() {
   }

   public static void ensureInit(Minecraft mc) {
      if (ready) {
         return;
      }
      image = new NativeImage(SIZE, SIZE, false);
      rounded = new NativeImage(SIZE, SIZE, false);
      texture = new DynamicTexture(() -> "boathud-hud-minimap", rounded);
      TextureManager tm = mc.getTextureManager();
      tm.register(TEX_ID, texture);
      ready = true;
   }

   public static boolean needsUpdate(Minecraft mc) {
      if (mc.player == null || mc.level == null) {
         return false;
      }
      ChunkPos cp = mc.player.chunkPosition();
      int y = mc.player.getBlockY();
      return !cp.equals(lastChunk) || y != lastY;
   }

   private static boolean isIce(Block b) {
      return b == Blocks.ICE || b == Blocks.PACKED_ICE || b == Blocks.BLUE_ICE || b == Blocks.FROSTED_ICE;
   }

   /** 重建地图：含上下 map_depth 层搜索，越深透明度越低 */
   public static void rebuild(Minecraft mc) {
      if (!ready || mc.player == null || mc.level == null) {
         return;
      }
      Level level = mc.level;
      ChunkPos center = mc.player.chunkPosition();
      int y0 = mc.player.getBlockY();
      lastChunk = center;
      lastY = y0;

      int minBlockX = center.getMinBlockX() - CHUNK_RADIUS * 16;
      int minBlockZ = center.getMinBlockZ() - CHUNK_RADIUS * 16;
      mapBlockPos = new BlockPos(minBlockX, y0, minBlockZ);

      int depth = Common.CONFIG == null ? 10 : Math.max(1, Common.CONFIG.map_depth);
      int colorChange = (int)Math.ceil(255.0 / depth);

      for (int px = 0; px < SIZE; px++) {
         int wx = minBlockX + px;
         for (int pz = 0; pz < SIZE; pz++) {
            int wz = minBlockZ + pz;
            int color = 0;

            // 1) 玩家同层
            if (isIce(level.getBlockState(new BlockPos(wx, y0, wz)).getBlock())
               && !level.getBlockState(new BlockPos(wx, y0 + 1, wz)).canOcclude()) {
               color = 0xFFF0F0F0;
            } else {
               // 2) 向下搜索
               for (int d = 1; d <= depth; d++) {
                  int yy = y0 - d;
                  if (isIce(level.getBlockState(new BlockPos(wx, yy, wz)).getBlock())
                     && !level.getBlockState(new BlockPos(wx, yy + 1, wz)).canOcclude()) {
                     int alpha = Math.max(0, Math.min(255, 255 - d * colorChange));
                     color = (alpha << 24) | 0x00FFFFFF;
                     break;
                  }
               }
               // 3) 向上搜索
               if (color == 0) {
                  for (int d = 1; d <= depth; d++) {
                     int yy = y0 + d;
                     if (isIce(level.getBlockState(new BlockPos(wx, yy, wz)).getBlock())
                        && !level.getBlockState(new BlockPos(wx, yy + 1, wz)).canOcclude()) {
                        int alpha = Math.max(0, Math.min(255, 255 - d * colorChange));
                        color = (alpha << 24) | 0x00FFFFFF;
                        break;
                     }
                  }
               }
            }
            image.setPixel(px, pz, color);
         }
      }
      roundAndUpload(mc);
   }

   private static double lastPx = 1e9;
   private static double lastPz = 1e9;
   private static boolean dirty = true;

   public static void markDirty() {
      dirty = true;
   }

   /**
    * 每帧调用：以【玩家在图中的实际位置】为圆心重新烘焙圆形蒙版并上传。
    * 这样玩家永远落在框的正中心，圆也不会偏出去。
    */
   public static void updateVisual(Minecraft mc) {
      if (!ready || mc.player == null) {
         return;
      }
      double px = mc.player.getX() - mapBlockPos.getX();
      double pz = mc.player.getZ() - mapBlockPos.getZ();
      if (!dirty && Math.abs(px - lastPx) < 0.2 && Math.abs(pz - lastPz) < 0.2) {
         return;
      }
      lastPx = px;
      lastPz = pz;
      dirty = false;

      float r = SIZE * 0.47F;
      float r2 = r * r;
      float fx = (float)px;
      float fz = (float)pz;
      for (int y = 0; y < SIZE; y++) {
         float dy = y + 0.5F - fz;
         for (int x = 0; x < SIZE; x++) {
            float dx = x + 0.5F - fx;
            rounded.setPixel(x, y, (dx * dx + dy * dy) <= r2 ? image.getPixel(x, y) : 0);
         }
      }
      texture.upload();
   }

   /** 兼容旧调用 */
   public static void roundAndUpload(Minecraft mc) {
      markDirty();
      updateVisual(mc);
   }
}