package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import java.util.Collection;

/**
 * 冰面样式切换：按配置三态控制内建资源包（Yes 始终 / Off 始终关闭 / Auto 上船才启用）。
 *
 * 内建包位置：jar 内 resourcepacks/ice_concrete/（目录名 = Identifier 的 path）
 *
 * 注意：切换资源包需要一次资源重载，重载会释放/重新抓取鼠标，
 * 导致按键状态可能残留（表现为“自动长按右键”）。因此：
 *   1) 上船后延迟若干 tick 再切换，避开刚上船的输入状态
 *   2) 重载完成后强制清空鼠标/移动键的按下状态
 */
public final class IceTrackMode {
   public static final Identifier PACK_ID = Identifier.fromNamespaceAndPath("wakespeed", "ice_concrete");

   /** 上船后等待的 tick 数，避开上船瞬间的输入 */
   private static final int SETTLE_TICKS = 12;

   private static Boolean applied = null;
   private static int settleTimer = 0;
   private static boolean warned = false;

   private IceTrackMode() {
   }

   public static void tick(Minecraft mc) {
      if (mc.player == null) {
         applied = null;
         settleTimer = 0;
         return;
      }

      ConfigEnums.ice_concrete mode;
      try {
         mode = Common.CONFIG.ice_concrete;
      } catch (Throwable t) {
         return;
      }
      if (mode == null) {
         return;
      }

      boolean onBoat = mc.player.getVehicle() instanceof AbstractBoat;
      boolean want;
      if (mode == ConfigEnums.ice_concrete.Yes) {
         want = true;
      } else if (mode == ConfigEnums.ice_concrete.Off) {
         want = false;
      } else {
         want = onBoat;
      }

      if (applied == null) {
         if (!want) {
            applied = Boolean.FALSE;
            return;
         }
      } else if (applied.booleanValue() == want) {
         settleTimer = 0;
         return;
      }

      // 上船切换时先等几 tick，避免刚上船就重载导致输入卡住
      if (want) {
         settleTimer++;
         if (settleTimer < SETTLE_TICKS) {
            return;
         }
      }
      settleTimer = 0;
      applied = Boolean.valueOf(want);

      try {
         PackRepository repo = mc.getResourcePackRepository();

         String target = null;
         Collection<String> ids = repo.getAvailableIds();
         for (String s : ids) {
            if (s != null && s.contains("ice_concrete")) {
               target = s;
               break;
            }
         }

         if (target == null) {
            if (!warned) {
               warned = true;
               System.out.println("[wakespeed] 找不到内建冰面包，可用 id: " + ids);
            }
            return;
         }

         boolean changed = want ? repo.addPack(target) : repo.removePack(target);
         System.out.println("[wakespeed] ice pack " + (want ? "enable" : "disable")
            + " id=" + target + " changed=" + changed);

         if (changed) {
            mc.reloadResourcePacks().thenRun(() -> mc.execute(IceTrackMode::clearStuckInput));
         }
      } catch (Throwable t) {
         System.out.println("[wakespeed] ice pack toggle failed: " + t);
      }
   }

   /** 资源重载后清掉可能残留的按下状态（防止“自动长按右键/左键”） */
   private static void clearStuckInput() {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.options == null) {
            return;
         }
         mc.options.keyUse.setDown(false);
         mc.options.keyAttack.setDown(false);
         mc.options.keyUp.setDown(false);
         mc.options.keyDown.setDown(false);
         mc.options.keyLeft.setDown(false);
         mc.options.keyRight.setDown(false);
         mc.options.keyJump.setDown(false);
         System.out.println("[wakespeed] input state cleared after reload");
      } catch (Throwable ignored) {
      }
   }

   public static boolean isApplied() {
      return Boolean.TRUE.equals(applied);
   }
}