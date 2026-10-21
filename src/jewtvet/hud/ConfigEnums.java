package jewtvet.hud;

public class ConfigEnums {
   public enum mod_enabled { Yes, No }
   public enum render_speed { Yes, No }
   public enum render_race_status { Yes, No }
   public enum render_map { Yes, No }
   public enum render_rpm { Yes, No }
   public enum render_drift { Yes, No }
   public enum rpm_style {
      Bar("\u6761\u72b6\u8868\uff08Bar\uff09"),
      Dial("\u5706\u8868\uff08Dial\uff09"),
      Meter("\u7535\u8868\uff08E-Meter\uff09");

      private final String label;
      rpm_style(String label) { this.label = label; }
      @Override public String toString() { return label; }
   }
   public enum rpm_jitter {
      Auto("\u81ea\u52a8\uff08Auto\uff09"),
      Off("\u5173\u95ed\u6296\u52a8\uff08Off\uff09");

      private final String label;
      rpm_jitter(String label) { this.label = label; }
      @Override public String toString() { return label; }
   }
   /** \u51b0\u9762\u6df7\u51dd\u571f\uff1aYes = \u59cb\u7ec8\u542f\u7528 / Off = \u59cb\u7ec8\u5173\u95ed / Auto = \u4e0a\u8239\u624d\u542f\u7528 */
   public enum ice_concrete {
      Yes("\u542f\u7528\uff08Yes\uff09"),
      Off("\u5173\u95ed\uff08Off\uff09"),
      Auto("\u81ea\u52a8\uff08Auto\uff09");

      private final String label;
      ice_concrete(String label) { this.label = label; }
      @Override public String toString() { return label; }
   }
   public enum gearbox {
      Auto("自动档（Auto）"),
      Manual("手动档（Manual）");
      private final String label;
      gearbox(String label) { this.label = label; }
      @Override public String toString() { return label; }
   }

   public enum driving_mode {
      Rally("拉力（Rally）"),
      Civilian("民用（Civilian）");
      private final String label;
      driving_mode(String label) { this.label = label; }
      @Override public String toString() { return label; }
   }

   public enum drive_input {
      Vanilla("原版WASD（Vanilla）"),
      Custom("自定义按键（Custom）");
      private final String label;
      drive_input(String label) { this.label = label; }
      @Override public String toString() { return label; }
   }

   public enum resize_map { Yes, No }
   public enum move_hp_lower { Yes, No }
   public enum render_hp { Yes, No }
   public enum render_hotbar { Yes, No }
}