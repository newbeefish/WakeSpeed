package jewtvet.boathud;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/**
 * 本模组全部可自定义键位（选项 -> 控制 -> 按键绑定 -> 浪速）。
 */
public final class GearKeys {
   private static final KeyMapping.Category CATEGORY =
      KeyMapping.Category.register(Identifier.fromNamespaceAndPath("wakespeed", "keys"));

   // 手动挡
   public static final KeyMapping SHIFT_UP = new KeyMapping(
      "key.wakespeed.shift_up", InputConstants.Type.KEYSYM, 88, CATEGORY);      // X
   public static final KeyMapping SHIFT_DOWN = new KeyMapping(
      "key.wakespeed.shift_down", InputConstants.Type.KEYSYM, 90, CATEGORY);    // Z
   public static final KeyMapping CLUTCH = new KeyMapping(
      "key.wakespeed.clutch", InputConstants.Type.KEYSYM, 67, CATEGORY);        // C

   // 自定义驾驶（默认与原版 WASD 相同）
   public static final KeyMapping THROTTLE = new KeyMapping(
      "key.wakespeed.throttle", InputConstants.Type.KEYSYM, 87, CATEGORY);      // W
   public static final KeyMapping BRAKE = new KeyMapping(
      "key.wakespeed.brake", InputConstants.Type.KEYSYM, 83, CATEGORY);         // S
   public static final KeyMapping STEER_LEFT = new KeyMapping(
      "key.wakespeed.steer_left", InputConstants.Type.KEYSYM, 65, CATEGORY);    // A
   public static final KeyMapping STEER_RIGHT = new KeyMapping(
      "key.wakespeed.steer_right", InputConstants.Type.KEYSYM, 68, CATEGORY);   // D

   // 功能
   public static final KeyMapping OPEN_SETTINGS = new KeyMapping(
      "key.wakespeed.settings", InputConstants.Type.KEYSYM, 298, CATEGORY);     // F9
   public static final KeyMapping OPEN_SETTINGS_ALT = new KeyMapping(
      "key.wakespeed.settings_alt", InputConstants.Type.KEYSYM, 299, CATEGORY); // F10
   public static final KeyMapping TOGGLE_GEARBOX = new KeyMapping(
      "key.wakespeed.toggle_gearbox", InputConstants.Type.KEYSYM, 71, CATEGORY);// G
   public static final KeyMapping TOGGLE_STYLE = new KeyMapping(
      "key.wakespeed.toggle_style", InputConstants.Type.KEYSYM, 72, CATEGORY);  // H
   public static final KeyMapping TOGGLE_EV = new KeyMapping(
      "key.wakespeed.toggle_ev", InputConstants.Type.KEYSYM, 86, CATEGORY);     // V

   private GearKeys() {
   }

   public static void register() {
      KeyMappingHelper.registerKeyMapping(SHIFT_UP);
      KeyMappingHelper.registerKeyMapping(SHIFT_DOWN);
      KeyMappingHelper.registerKeyMapping(CLUTCH);
      KeyMappingHelper.registerKeyMapping(THROTTLE);
      KeyMappingHelper.registerKeyMapping(BRAKE);
      KeyMappingHelper.registerKeyMapping(STEER_LEFT);
      KeyMappingHelper.registerKeyMapping(STEER_RIGHT);
      KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS);
      KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS_ALT);
      KeyMappingHelper.registerKeyMapping(TOGGLE_GEARBOX);
      KeyMappingHelper.registerKeyMapping(TOGGLE_STYLE);
      KeyMappingHelper.registerKeyMapping(TOGGLE_EV);
   }
}