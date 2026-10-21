package jewtvet.boathud;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

public class Common implements ClientModInitializer {
   public static final String MODID = "wakespeed";
   /** F10 打开音效设置面板 */
   private static final int KEY_CONFIG = 299;

   public static BoatEngineConfig CONFIG;

   private static boolean prevConfigKey = false;

   @Override
   public void onInitializeClient() {
      AutoConfig.register(BoatEngineConfig.class, GsonConfigSerializer::new);
      GearKeys.register();
      CONFIG = AutoConfig.getConfigHolder(BoatEngineConfig.class).getConfig();

      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         BoatHudSound.onClientTick(mc);

         if (mc.player == null || mc.level == null) {
            return;
         }
         boolean playing = mc.mouseHandler.isMouseGrabbed();
         boolean cfgKey = playing && InputConstants.isKeyDown(mc.getWindow(), KEY_CONFIG);
         if (cfgKey && !prevConfigKey) {
            Screens.open(mc, jewtvet.hud.SettingsScreen.create(null));
         }
         prevConfigKey = cfgKey;
      });
   }

   /** 供音效类读取配置（可能尚未初始化） */
   public static BoatEngineConfig cfg() {
      if (CONFIG == null) {
         CONFIG = AutoConfig.getConfigHolder(BoatEngineConfig.class).getConfig();
      }
      return CONFIG;
   }
}