package jewtvet.hud;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 合并设置面板：一个界面，两个大类（音效 / 界面）。
 * 按 F9 或 F10 都能打开。
 */
public final class SettingsScreen {
   private SettingsScreen() {
   }

   public static Screen create(Screen parent) {
      BoatHudConfig hud = Common.CONFIG;
      jewtvet.boathud.BoatEngineConfig snd = jewtvet.boathud.Common.cfg();

      ConfigBuilder builder = ConfigBuilder.create()
         .setParentScreen(parent)
         .setTitle(Component.literal("BoatHUD \u8bbe\u7f6e\uff08Settings\uff09"))
         .setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(BoatHudConfig.class).save();
            AutoConfig.getConfigHolder(jewtvet.boathud.BoatEngineConfig.class).save();
         });

      ConfigEntryBuilder eb = builder.entryBuilder();

      // ==================== 一、音效（Sound） ====================
      ConfigCategory sound = builder.getOrCreateCategory(Component.literal("\u4e00\u3001\u97f3\u6548\uff08Sound\uff09"));

      sound.addEntry(eb.startBooleanToggle(
            Component.literal("\u7535\u8f66\u6a21\u5f0f\uff08EV mode\uff09- \u5f15\u64ce\u58f0\u6362\u6210\u7535\u673a\u58f0"),
            snd.ev_mode)
         .setDefaultValue(false)
         .setSaveConsumer(v -> snd.ev_mode = v)
         .build());
      sound.addEntry(eb.startTextDescription(Component.literal(
         "\u5f00\uff1a\u7535\u673a\u9ad8\u9891\u54f2\u53eb\uff0c\u65e0\u6da1\u8f6e/\u6cc4\u538b\u9600/\u79bb\u5408/\u6302\u6321\n" +
         "\u5173\uff1a\u71c3\u6cb9\u5f15\u64ce + \u6da1\u8f6e + \u6cc4\u538b\u9600 + \u79bb\u5408 + \u6302\u6321"))
         .build());

      sound.addEntry(intSlider(eb, "\u5f15\u64ce / \u7535\u673a\uff08Engine / Motor %\uff09", snd.volume_engine, 0, 200, v -> snd.volume_engine = v, 100));
      sound.addEntry(intSlider(eb, "\u6da1\u8f6e\uff08Turbo %\uff09", snd.volume_turbo, 0, 200, v -> snd.volume_turbo = v, 100));
      sound.addEntry(intSlider(eb, "\u6cc4\u538b\u9600\uff08Blow-off %\uff09", snd.volume_blowoff, 0, 200, v -> snd.volume_blowoff = v, 100));
      sound.addEntry(intSlider(eb, "\u79bb\u5408\uff08Clutch %\uff09", snd.volume_clutch, 0, 200, v -> snd.volume_clutch = v, 100));
      sound.addEntry(intSlider(eb, "\u6302\u6321\uff08Gear shift %\uff09", snd.volume_gear, 0, 200, v -> snd.volume_gear = v, 100));
      sound.addEntry(intSlider(eb, "\u6f02\u79fb\u80ce\u53eb\uff08Drift %\uff09", snd.volume_drift, 0, 200, v -> snd.volume_drift = v, 100));
      sound.addEntry(intSlider(eb, "\u5f15\u64ce / \u7535\u673a\u97f3\u9ad8\uff08Pitch %\uff09", snd.pitch_engine, 50, 150, v -> snd.pitch_engine = v, 100));
      sound.addEntry(eb.startBooleanToggle(
            Component.literal("\u5c4f\u853d\u539f\u7248\u5212\u6c34\u58f0\uff08Mute vanilla boat sounds\uff09"),
            snd.mute_vanilla_boat)
         .setDefaultValue(true)
         .setSaveConsumer(v -> snd.mute_vanilla_boat = v)
         .build());

      // ==================== 二、界面（HUD） ====================
      ConfigCategory ui = builder.getOrCreateCategory(Component.literal("\u4e8c\u3001\u754c\u9762\uff08HUD\uff09"));

      ui.addEntry(eb.startBooleanToggle(
            Component.literal("\u8d5b\u8f66\u89c6\u89d2\uff08Racing camera\uff09"),
            boatcam.config.BoatCamConfig.getConfig().boatMode)
         .setDefaultValue(true)
         .setSaveConsumer(v -> {
            boatcam.config.BoatCamConfig.getConfig().boatMode = v;
            boatcam.config.BoatCamConfig.getConfig().save();
         })
         .build());
      ui.addEntry(eb.startTextDescription(Component.literal(
         "\u5f00\u542f\uff1a\u955c\u5934\u8ddf\u968f\u8239\u5934\u65b9\u5411\uff0c\u9ad8\u901f\u8fc7\u5f2f\u66f4\u7a33\uff08BoatCam\uff09\n" +
         "\u5173\u95ed\uff1a\u6062\u590d\u539f\u7248\u955c\u5934\u884c\u4e3a"))
         .build());
      ui.addEntry(eb.startIntSlider(
            Component.literal("\u955c\u5934\u5e73\u6ed1\u5ea6\uff08Camera smoothness\uff09"),
            boatcam.config.BoatCamConfig.getConfig().smoothness, 1, 100)
         .setDefaultValue(50)
         .setSaveConsumer(v -> {
            boatcam.config.BoatCamConfig.getConfig().smoothness = v;
            boatcam.config.BoatCamConfig.getConfig().save();
         })
         .build());

      ui.addEntry(eb.startEnumSelector(
            Component.literal("\u8f6c\u901f\u8868\u6837\u5f0f\uff08RPM gauge style\uff09"),
            ConfigEnums.rpm_style.class, hud.rpm_style)
         .setDefaultValue(ConfigEnums.rpm_style.Bar)
         .setSaveConsumer(v -> hud.rpm_style = v)
         .build());
      ui.addEntry(eb.startEnumSelector(
            Component.literal("\u51b0\u9762\u6df7\u51dd\u571f\uff08Ice as concrete\uff09"),
            ConfigEnums.ice_concrete.class, hud.ice_concrete)
         .setDefaultValue(ConfigEnums.ice_concrete.Auto)
         .setSaveConsumer(v -> hud.ice_concrete = v)
         .build());
      ui.addEntry(eb.startEnumSelector(
            Component.literal("\u8f6c\u901f\u6296\u52a8\uff08RPM jitter\uff09"),
            ConfigEnums.rpm_jitter.class, hud.rpm_jitter)
         .setDefaultValue(ConfigEnums.rpm_jitter.Auto)
         .setSaveConsumer(v -> hud.rpm_jitter = v)
         .build());

      ui.addEntry(enumEntry(eb, "\u663e\u793a\u8f6c\u901f\u8868\uff08Show RPM gauge\uff09",
         ConfigEnums.render_rpm.class, hud.render_rpm, v -> hud.render_rpm = v, ConfigEnums.render_rpm.Yes));
      ui.addEntry(enumEntry(eb, "\u663e\u793a\u901f\u5ea6\u8868\uff08Show speedometer\uff09",
         ConfigEnums.render_speed.class, hud.render_speed, v -> hud.render_speed = v, ConfigEnums.render_speed.Yes));
      ui.addEntry(enumEntry(eb, "\u663e\u793a\u5c0f\u5730\u56fe\uff08Show minimap\uff09",
         ConfigEnums.render_map.class, hud.render_map, v -> hud.render_map = v, ConfigEnums.render_map.Yes));
      ui.addEntry(enumEntry(eb, "\u663e\u793a\u6bd4\u8d5b\u72b6\u6001\uff08Show race status\uff09",
         ConfigEnums.render_race_status.class, hud.render_race_status, v -> hud.render_race_status = v, ConfigEnums.render_race_status.Yes));
      ui.addEntry(enumEntry(eb, "\u663e\u793a\u6f02\u79fb\u8ba1\u65f6\uff08Show drift meter\uff09",
         ConfigEnums.render_drift.class, hud.render_drift, v -> hud.render_drift = v, ConfigEnums.render_drift.Yes));

      ui.addEntry(intSlider(eb, "\u8f6c\u901f\u8868 X\uff08RPM gauge X %\uff09", hud.rpm_x, 0, 100, v -> hud.rpm_x = v, 0));
      ui.addEntry(intSlider(eb, "\u8f6c\u901f\u8868 Y\uff08RPM gauge Y %\uff09", hud.rpm_y, 0, 100, v -> hud.rpm_y = v, 0));
      ui.addEntry(intSlider(eb, "\u901f\u5ea6\u8868 X\uff08Speedometer X %\uff09", hud.speed_x, 0, 100, v -> hud.speed_x = v, 100));
      ui.addEntry(intSlider(eb, "\u901f\u5ea6\u8868 Y\uff08Speedometer Y %\uff09", hud.speed_y, 0, 100, v -> hud.speed_y = v, 100));
      ui.addEntry(intSlider(eb, "\u5c0f\u5730\u56fe X\uff08Minimap X %\uff09", hud.map_x, 0, 100, v -> hud.map_x = v, 0));
      ui.addEntry(intSlider(eb, "\u5c0f\u5730\u56fe Y\uff08Minimap Y %\uff09", hud.map_y, 0, 100, v -> hud.map_y = v, 100));
      ui.addEntry(intSlider(eb, "\u6bd4\u8d5b\u72b6\u6001 X\uff08Race status X %\uff09", hud.status_x, 0, 100, v -> hud.status_x = v, 100));
      ui.addEntry(intSlider(eb, "\u6bd4\u8d5b\u72b6\u6001 Y\uff08Race status Y %\uff09", hud.status_y, 0, 100, v -> hud.status_y = v, 0));
      ui.addEntry(intSlider(eb, "\u5c0f\u5730\u56fe\u6df1\u5ea6\uff08Minimap depth \u00b1blocks\uff09", hud.map_depth, 1, 32, v -> hud.map_depth = v, 10));

      return builder.build();
   }

   private static AbstractConfigListEntry<?> intSlider(
      ConfigEntryBuilder eb, String label, int value, int min, int max,
      java.util.function.IntConsumer setter, int def) {
      return eb.startIntSlider(Component.literal(label), value, min, max)
         .setDefaultValue(def).setSaveConsumer(setter::accept).build();
   }

   private static <T extends Enum<?>> AbstractConfigListEntry<?> enumEntry(
      ConfigEntryBuilder eb, String label, Class<T> type, T current,
      java.util.function.Consumer<T> setter, T def) {
      return eb.startEnumSelector(Component.literal(label), type, current)
         .setDefaultValue(def).setSaveConsumer(setter).build();
   }
}
