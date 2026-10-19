package jewtvet.hud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

@Config(name = "wakespeed")
public class BoatHudConfig implements ConfigData {
   @Tooltip public ConfigEnums.mod_enabled mod_enabled = ConfigEnums.mod_enabled.Yes;
   @Tooltip public ConfigEnums.render_speed render_speed = ConfigEnums.render_speed.Yes;
   @Tooltip public ConfigEnums.render_drift render_drift = ConfigEnums.render_drift.Yes;
   @Tooltip public ConfigEnums.rpm_style rpm_style = ConfigEnums.rpm_style.Bar;
   /** \u8f6c\u901f\u6296\u52a8\uff1aOff = \u5b8c\u5168\u4e0d\u6296\uff08\u7535\u673a/\u7535\u8868\u63a8\u8350\uff09 */
   @Tooltip public ConfigEnums.rpm_jitter rpm_jitter = ConfigEnums.rpm_jitter.Auto;
   /** \u9a91\u8239\u65f6\u81ea\u52a8\u542f\u7528\u51b0\u9762\u6df7\u51dd\u571f\u5e16\u56fe\uff08\u4e0b\u8239\u81ea\u52a8\u5173\u95ed\uff09 */
   @Tooltip public ConfigEnums.ice_concrete ice_concrete = ConfigEnums.ice_concrete.Auto;
   @Tooltip public ConfigEnums.render_rpm render_rpm = ConfigEnums.render_rpm.Yes;
   @Tooltip public ConfigEnums.render_map render_map = ConfigEnums.render_map.Yes;
   @Tooltip public ConfigEnums.render_race_status render_race_status = ConfigEnums.render_race_status.Yes;
   @Tooltip public ConfigEnums.resize_map resize_map = ConfigEnums.resize_map.No;
   @Tooltip public ConfigEnums.move_hp_lower move_hp_lower = ConfigEnums.move_hp_lower.Yes;
   @Tooltip public ConfigEnums.render_hp render_hp = ConfigEnums.render_hp.Yes;
   @Tooltip public ConfigEnums.render_hotbar render_hotbar = ConfigEnums.render_hotbar.No;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int speed_x = 100;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int speed_y = 100;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int status_x = 100;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int status_y = 0;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int map_x = 0;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int map_y = 100;
   @Tooltip @BoundedDiscrete(min = 1L, max = 32L) public int map_depth = 10;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int rpm_x = 0;
   @Tooltip @BoundedDiscrete(min = 0L, max = 100L) public int rpm_y = 0;
}