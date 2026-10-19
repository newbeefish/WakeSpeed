package jewtvet.hud;

import com.mojang.blaze3d.platform.InputConstants;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

public class Common implements ClientModInitializer {
   public static final String MODID = "wakespeed";
   private static final int KEY_RESET = 82;    // R
   /** F9 打开配置面板 */
   private static final int KEY_CONFIG = 298;

   public static BoatHudConfig CONFIG;
   public static Minecraft client;
   public static HudData hudData = new HudData();
   public static HudRenderer hudRenderer;

   private static boolean prevResetKey = false;
   private static boolean prevConfigKey = false;
   private static boolean wasRiding = false;

   @Override
   public void onInitializeClient() {
      AutoConfig.register(BoatHudConfig.class, GsonConfigSerializer::new);

      // 内建冰面资源包（默认关闭，由 IceTrackMode 按配置切换）
      boolean ok = ResourceManagerHelper.registerBuiltinResourcePack(
         IceTrackMode.PACK_ID,
         FabricLoader.getInstance().getModContainer("wakespeed").orElseThrow(),
         Component.literal("BoatHud \u51b0\u9762\u6df7\u51dd\u571f\uff08Ice as Concrete\uff09"),
         ResourcePackActivationType.NORMAL);
      System.out.println("[boathud] builtin ice pack registered=" + ok);

      CONFIG = AutoConfig.getConfigHolder(BoatHudConfig.class).getConfig();
      client = Minecraft.getInstance();
      hudRenderer = new HudRenderer(client);

      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         IceTrackMode.tick(mc);
         if (mc.player == null || mc.level == null) {
            return;
         }

         boolean riding = mc.player.getVehicle() instanceof AbstractBoat;

         // 下船：重置所有 HUD 状态（转速、挡位、圈速、速度、漂移）
         if (!riding && wasRiding) {
            LapTimer.reset();
            hudData.reset();
            DriftMeter.reset();
         }
         wasRiding = riding;

         hudData.update();

         boolean playing = mc.mouseHandler.isMouseGrabbed();
         boolean resetKey = playing && InputConstants.isKeyDown(mc.getWindow(), KEY_RESET);
         if (resetKey && !prevResetKey) {
            LapTimer.reset();
            mc.player.sendSystemMessage(Component.literal("BoatHud: lap timer reset"));
         }
         prevResetKey = resetKey;

         boolean cfgKey = playing && InputConstants.isKeyDown(mc.getWindow(), KEY_CONFIG);
         if (cfgKey && !prevConfigKey) {
            Screens.open(mc, SettingsScreen.create(null));
         }
         prevConfigKey = cfgKey;

         if (mc.player.getVehicle() instanceof AbstractBoat boat) {
            DriftMeter.tick(boat.getX(), boat.getZ(), hudData.speed, hudData.driftAngle);
            boolean lap = LapTimer.tick(mc.level, boat.getX(), boat.getY(), boat.getZ());
            hudData.time_current_lap = LapTimer.current;
            hudData.time_prev_lap = LapTimer.previous;
            hudData.isRaceType = false;
            if (lap) {
               hudData.track_times.add(1);
            }
         }
      });

      HudElementRegistry.addLast(
         Identifier.fromNamespaceAndPath(MODID, "hud"),
         (g, delta) -> {
            if (client.player == null) return;
            if (!(client.player.getVehicle() instanceof AbstractBoat)) return;
            hudRenderer.render(g, delta.getGameTimeDeltaPartialTick(true));
         }
      );
   }
}