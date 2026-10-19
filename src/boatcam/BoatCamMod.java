package boatcam;

import boatcam.config.BoatCamConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;

public final class BoatCamMod implements ClientModInitializer {
   private static BoatCamMod INSTANCE;
   private final KeyMapping.Category KEY_BINDING_CATEGORY =
      new KeyMapping.Category(Identifier.fromNamespaceAndPath("boatcam", "boatcam"));
   private final KeyMapping MENU = new KeyMapping("key.boatcam.menu", InputConstants.Type.KEYSYM, 66, this.KEY_BINDING_CATEGORY);
   private final KeyMapping TOGGLE = new KeyMapping("key.boatcam.toggle", InputConstants.Type.KEYSYM, -1, this.KEY_BINDING_CATEGORY);
   private final KeyMapping LOOK_BEHIND = new KeyMapping("key.boatcam.lookbehind", InputConstants.Type.KEYSYM, -1, this.KEY_BINDING_CATEGORY);
   private final KeyMapping LOOK_LEFT = new KeyMapping("key.boatcam.lookleft", InputConstants.Type.KEYSYM, -1, this.KEY_BINDING_CATEGORY);
   private final KeyMapping LOOK_RIGHT = new KeyMapping("key.boatcam.lookright", InputConstants.Type.KEYSYM, -1, this.KEY_BINDING_CATEGORY);
   private CameraType perspective = null;
   private CameraType previousPerspective = null;
   private Vec3 boatPos = Vec3.ZERO;
   private float previousYaw;
   private boolean unfixedCameraActive = false;
   private boolean lookingBehind = false;

   public BoatCamMod() {
      INSTANCE = this;
   }

   @Override
   public void onInitializeClient() {
      try {
         BoatCamConfig.load();
      } catch (Exception e) {
         System.out.println("Could not load config.");
         throw new RuntimeException(e);
      }

      KeyMappingHelper.registerKeyMapping(this.MENU);
      KeyMappingHelper.registerKeyMapping(this.TOGGLE);
      KeyMappingHelper.registerKeyMapping(this.LOOK_BEHIND);
      KeyMappingHelper.registerKeyMapping(this.LOOK_LEFT);
      KeyMappingHelper.registerKeyMapping(this.LOOK_RIGHT);
      ClientTickEvents.START_LEVEL_TICK.register(this::onClientStartWorldTick);
   }

   private void onClientStartWorldTick(ClientLevel world) {
      Minecraft client = Minecraft.getInstance();
      if (this.MENU.consumeClick()) {
         client.player.sendSystemMessage(Component.literal("BoatCam: edit config/boatcam.json5 (no GUI in this build)"));
      } else {
         if (this.TOGGLE.consumeClick()) {
            BoatCamConfig.getConfig().toggleBoatMode();
            client.player.sendSystemMessage(
               Component.literal(BoatCamConfig.getConfig().boatMode ? "Boat mode" : "Normal mode")
                  .withStyle(style -> style.withColor(ChatFormatting.GREEN))
            );
         }

         if (BoatCamConfig.getConfig().boatMode && client.player.getVehicle() instanceof AbstractBoat boat) {
            this.calculateYaw(client.player, boat);
            if (this.unfixedCameraActive != this.shouldOverrideCamera(boat)) {
               this.unfixedCameraActive = !this.unfixedCameraActive;
               if (this.unfixedCameraActive && BoatCamConfig.getConfig().fixedPitch) {
                  if (this.lookingBehind) {
                     client.player.setXRot((float)(-BoatCamConfig.getConfig().pitch));
                  } else {
                     client.player.setXRot((float)BoatCamConfig.getConfig().pitch);
                  }
               }
            }

            if (this.perspective == null) {
               this.perspective = client.options.getCameraType();
               switch (BoatCamConfig.getConfig().getPerspective()) {
                  case FIRST_PERSON:
                     client.options.setCameraType(CameraType.FIRST_PERSON);
                     break;
                  case THIRD_PERSON:
                     if (this.lookingBehind) {
                        client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                     } else {
                        client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                     }
               }

               if (BoatCamConfig.getConfig().fixedPitch) {
                  if (this.lookingBehind) {
                     client.player.setXRot((float)(-BoatCamConfig.getConfig().pitch));
                  } else {
                     client.player.setXRot((float)BoatCamConfig.getConfig().pitch);
                  }
               } else if (this.lookingBehind) {
                  client.player.setXRot((float)(-BoatCamConfig.getConfig().pitch));
               }
            }
         } else if (this.perspective != null && !this.lookingBehind) {
            Minecraft.getInstance().options.setCameraType(this.perspective);
            this.perspective = null;
         }

         if (this.LOOK_BEHIND.isDown() != this.lookingBehind) {
            this.lookingBehind = this.LOOK_BEHIND.isDown();
            this.invertPitch();
            this.toggleLookBehindPerspective();
         }
      }
   }

   private void toggleLookBehindPerspective() {
      Minecraft client = Minecraft.getInstance();
      if (this.lookingBehind) {
         this.previousPerspective = client.options.getCameraType();
         client.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
      } else {
         client.options.setCameraType(this.previousPerspective);
      }
   }

   private void invertPitch() {
      LocalPlayer player = Minecraft.getInstance().player;
      player.setXRot(-player.getXRot());
   }

   private void calculateYaw(LocalPlayer player, AbstractBoat boat) {
      float yaw = boat.getYRot();
      if (!this.shouldOverrideCamera(boat)) {
         this.previousYaw = yaw;
         this.boatPos = boat.position();
      } else {
         double dx = boat.getX() - this.boatPos.x;
         double dz = boat.getZ() - this.boatPos.z;
         float directionOffset = 0.0F;
         if (BoatCamConfig.getConfig().snapSidewaysView) {
            if (this.LOOK_LEFT.isDown()) {
               yaw -= 90.0F;
            } else if (this.LOOK_RIGHT.isDown()) {
               yaw += 90.0F;
            } else {
               if (dx != 0.0 || dz != 0.0) {
                  float vel = (float)Math.hypot(dz, dx);
                  float direction = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
                  float t = Math.min(1.0F, vel / 3.0F);
                  yaw = AngleUtil.lerp(t, yaw, direction);
               }

               yaw = AngleUtil.lerp(BoatCamConfig.getConfig().getSmoothness(), this.previousYaw, yaw);
            }
         } else {
            if (this.LOOK_LEFT.isDown()) {
               yaw -= 90.0F;
               directionOffset = -90.0F;
            } else if (this.LOOK_RIGHT.isDown()) {
               yaw += 90.0F;
               directionOffset = 90.0F;
            }

            if (dx != 0.0 || dz != 0.0) {
               float vel = (float)Math.hypot(dz, dx);
               float direction = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
               float t = Math.min(1.0F, vel / 3.0F);
               yaw = AngleUtil.lerp(t, yaw, direction + directionOffset);
            }

            yaw = AngleUtil.lerp(BoatCamConfig.getConfig().getSmoothness(), this.previousYaw, yaw);
         }

         player.setYRot(yaw);
         this.previousYaw = yaw;
         this.boatPos = boat.position();
      }
   }

   public boolean onLookDirectionChanging(double dx, double dy) {
      LocalPlayer player = Minecraft.getInstance().player;
      if (!(player.getVehicle() instanceof AbstractBoat b)) {
         return false;
      } else if (BoatCamConfig.getConfig().boatMode
         && this.shouldOverrideCamera(b)
         && (dx != 0.0 || BoatCamConfig.getConfig().fixedPitch && dy != 0.0)) {
         player.turn(0.0, BoatCamConfig.getConfig().fixedPitch ? 0.0 : dy);
         return true;
      } else {
         return false;
      }
   }

   boolean shouldOverrideCamera(AbstractBoat boat) {
      return !BoatCamConfig.getConfig().stationaryLookAround
         || this.LOOK_LEFT.isDown()
         || this.LOOK_RIGHT.isDown()
         || boat.getDeltaMovement().lengthSqr() >= 1.0E-4;
   }

   public static BoatCamMod instance() {
      return INSTANCE;
   }
}
