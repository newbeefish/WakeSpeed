package jewtvet.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

/** F9 一级入口：设置 / 按键设置。 */
public class RootScreen extends Screen {
   private final Screen parent;

   public RootScreen(Screen parent) {
      super(Component.literal("WakeSpeed 浪速"));
      this.parent = parent;
   }

   public static RootScreen create(Screen parent) {
      return new RootScreen(parent);
   }

   @Override
   protected void init() {
      int cx = this.width / 2;
      int cy = this.height / 2;
      this.addRenderableWidget(Button.builder(Component.literal("设置（Settings）"),
            b -> Minecraft.getInstance().setScreenAndShow(SettingsScreen.create(this)))
         .bounds(cx - 110, cy - 34, 220, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("按键设置（Key Binds）"),
            b -> Minecraft.getInstance().setScreenAndShow(
               new KeyBindsScreen(this, Minecraft.getInstance().options)))
         .bounds(cx - 110, cy - 8, 220, 20).build());
      this.addRenderableWidget(Button.builder(Component.literal("关闭（Close）"),
            b -> this.onClose())
         .bounds(cx - 110, cy + 18, 220, 20).build());
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(g, mouseX, mouseY, partialTick);
      var font = Minecraft.getInstance().font;
      g.text(font, this.title, this.width / 2 - font.width(this.title) / 2, this.height / 2 - 76, 0xFFFFFFFF, false);
      Component hint = Component.literal("F9 / F10 打开（可在按键设置里改）");
      g.text(font, hint, this.width / 2 - font.width(hint) / 2, this.height / 2 - 60, 0xFFAAAAAA, false);
   }

   @Override
   public void onClose() {
      Minecraft.getInstance().setScreenAndShow(this.parent);
   }
}