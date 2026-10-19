package jewtvet.hud;

import jewtvet.boathud.BoatHudSound;

/**
 * HUD 读取音效状态。
 * 合并成同一个 mod 后直接访问字段，不再走反射（少一层失败可能）。
 */
public final class EngineLink {
   private EngineLink() {
   }

   /** @return 0..1 的转速 */
   public static float readRpm() {
      return BoatHudSound.rpm;
   }

   /** @return 挡位（-1=倒档 0=空挡 1..6 前进） */
   public static int readGear() {
      return BoatHudSound.gear;
   }

   /** @return 涡轮转速 0..1 */
   public static float readTurbo() {
      return BoatHudSound.turbo;
   }

   /** @return 是否电车模式 */
   public static boolean readEvMode() {
      return BoatHudSound.evMode;
   }
}