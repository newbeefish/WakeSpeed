package boatcam.config;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BoatCamConfig {
   private static BoatCamConfig INSTANCE;
   private static final Path CONFIG_PATH = Path.of("config", "boatcam.json5");
   public boolean boatMode = true;
   public boolean stationaryLookAround = true;
   public int smoothness = 50;
   public BoatCamConfig.Perspective perspective = BoatCamConfig.Perspective.THIRD_PERSON;
   public boolean fixedPitch = false;
   public int pitch = 25;
   public boolean turnLimitDisabled = true;
   public boolean snapSidewaysView = false;

   private BoatCamConfig() {
   }

   public void validatePostLoad() {
      this.smoothness = Math.clamp(this.smoothness, 1, 100);
      this.pitch = Math.clamp(this.pitch, -90, 90);
      if (this.perspective == null) {
         this.perspective = BoatCamConfig.Perspective.THIRD_PERSON;
      }
   }

   public float getSmoothness() {
      return this.smoothness / 100.0F;
   }

   public void toggleBoatMode() {
      this.boatMode = !this.boatMode;
      this.save();
   }

   public BoatCamConfig.Perspective getPerspective() {
      return this.perspective;
   }

   public boolean isTurnLimitDisabled() {
      return this.turnLimitDisabled;
   }

   public void save() {
      try {
         if (Files.notExists(CONFIG_PATH)) {
            Files.createFile(CONFIG_PATH);
         }

         Gson gson = new Gson();
         Files.writeString(CONFIG_PATH, gson.toJson(this));
      } catch (Exception e) {
         System.err.println("Could not write config: " + e.getMessage());
      }
   }

   public static void load() throws IOException {
      if (Files.notExists(CONFIG_PATH)) {
         INSTANCE = new BoatCamConfig();
      } else {
         Gson gson = new Gson();
         INSTANCE = (BoatCamConfig)gson.fromJson(Files.readString(CONFIG_PATH), BoatCamConfig.class);
         INSTANCE.validatePostLoad();
      }
   }

   public static BoatCamConfig getConfig() {
      return INSTANCE;
   }

   public enum Perspective {
      NONE,
      FIRST_PERSON,
      THIRD_PERSON;
   }
}
