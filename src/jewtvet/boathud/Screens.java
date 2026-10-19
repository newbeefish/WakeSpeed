package jewtvet.boathud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** 打开界面的跨版本兼容封装：26.1.2 用 setScreen，26.2 只有 setScreenAndShow */
public final class Screens {
   private Screens() {
   }

   public static void open(Minecraft mc, Screen screen) {
      try {
         Minecraft.class.getMethod("setScreenAndShow", Screen.class).invoke(mc, screen);
         return;
      } catch (Throwable ignored) {
      }
      try {
         Minecraft.class.getMethod("setScreen", Screen.class).invoke(mc, screen);
      } catch (Throwable ignored) {
      }
   }
}